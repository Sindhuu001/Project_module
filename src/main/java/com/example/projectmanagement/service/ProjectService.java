package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ProjectIdName;
import com.example.projectmanagement.ExternalDTO.ProjectTasksDto;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.config.ProjectStatusProperties;
import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.exception.ValidationException;
import com.example.projectmanagement.repository.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final UserService userService;
    private final ProjectStatusProperties projectStatusProperties;
    private final StatusService statusService;
    private final StatusRepository statusRepository;
    private final RiskStatusService riskStatusService;
    private final SprintRepository sprintRepository;
    private final RiskRepository riskRepository;
    private final RiskStatusRepository riskStatusRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final RiskLinkRepository riskLinkRepository;

    /**
     * Create project with validation and default statuses
     */
    public ProjectDto createProject(ProjectDto projectDto) {

    List<String> errors = new ArrayList<>();
    System.out.println("********Entering Create Service file ********");

    /* =====================
       BASIC VALIDATIONS
       ===================== */
    if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
        errors.add("Project name must not be empty.");
    }

    if (projectDto.getProjectKey() == null || projectDto.getProjectKey().trim().isEmpty()) {
        errors.add("Project key must be provided.");
    } else if (projectRepository.existsByProjectKey(projectDto.getProjectKey())) {
        errors.add("Project with key " + projectDto.getProjectKey() + " already exists.");
    }

    if (projectDto.getClientId() == null) {
        errors.add("Client ID is required.");
    }

    if (projectDto.getStartDate() == null) {
        errors.add("Start date is required.");
    }

    if (projectDto.getStartDate() != null
            && projectDto.getEndDate() != null
            && projectDto.getStartDate().isAfter(projectDto.getEndDate())) {
        errors.add("Start date cannot be after end date.");
    }

    /* =====================
       OWNER VALIDATION (UMS)
       ===================== */
    UserDto owner;
    try {
        owner = userService.getUserWithRoles(projectDto.getOwnerId());
        if (owner == null) {
            errors.add("Valid owner ID is required.");
        }
    } catch (Exception e) {
        errors.add("Valid owner ID is required.");
        owner = null;
    }

    if (!errors.isEmpty()) {
        throw new ValidationException(errors);
    }

    /* =====================
       DTO → ENTITY MAPPING
       ===================== */
    Project project = modelMapper.map(projectDto, Project.class);

    // Mandatory fields
    project.setClientId(projectDto.getClientId());
    project.setOwnerId(owner.getId());

    // Defaults (entity-safe)
    project.setStatus(
            projectDto.getStatus() != null
                    ? projectDto.getStatus()
                    : Project.ProjectStatus.ACTIVE
    );

    project.setCurrentStage(
            projectDto.getCurrentStage() != null
                    ? projectDto.getCurrentStage()
                    : Project.ProjectStage.INITIATION
    );

    // Members
    project.setMemberIds(
            projectDto.getMemberIds() != null
                    ? new HashSet<>(projectDto.getMemberIds())
                    : new HashSet<>()
    );

    // Optional ownership fields
    project.setRmId(projectDto.getRmId());
    project.setDeliveryOwnerId(projectDto.getDeliveryOwnerId());

    // Delivery / Risk / Priority
    project.setDeliveryModel(projectDto.getDeliveryModel());
    project.setPrimaryLocation(projectDto.getPrimaryLocation());
    project.setRiskLevel(projectDto.getRiskLevel());
    project.setRiskLevelUpdatedAt(projectDto.getRiskLevelUpdatedAt());
    project.setPriorityLevel(projectDto.getPriorityLevel());

    // Budget
    project.setProjectBudget(projectDto.getProjectBudget());
    project.setProjectBudgetCurrency(projectDto.getProjectBudgetCurrency());

    System.out.println("****************Created project entity: " + project);

    /* =====================
       SAVE PROJECT
       ===================== */
    Project savedProject = projectRepository.save(project);
    System.out.println("**************Saved Project: " + savedProject);

    /* =====================
       DEFAULT STATUSES
       ===================== */
    projectStatusProperties.getDefaultStatuses().forEach(statusProperty -> {
        StatusDto defaultStatusDto = new StatusDto();
        defaultStatusDto.setName(statusProperty.getName());
        statusService.addStatus(savedProject.getId(), defaultStatusDto);
    });

    /* =====================
       DEFAULT RISK STATUSES
       ===================== */
    riskStatusService.createDefaultStatusesForProject(savedProject.getId());

    return convertToDto(savedProject);
}


    @Transactional(readOnly = true)
    public Long getProjectCount() {
        return projectRepository.countByStatus(Project.ProjectStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public ProjectRiskSummaryDTO getProjectRisk(Long projectId) {

        ProjectRiskSummaryDTO dto = new ProjectRiskSummaryDTO();
        dto.setProjectId(projectId);

        // 1️⃣ Fetch ACTIVE sprint
//        Sprint activeSprint =
//                sprintRepository.findActiveSprintByProjectId(projectId)
//                        .orElse(null);

        Sprint activeSprint = sprintRepository
                .findFirstByProjectIdAndStatus(projectId, Sprint.SprintStatus.ACTIVE)
                .orElse(null);

        if (activeSprint == null) {
            dto.setRiskHealth(ProjectRiskSummaryDTO.RiskHealth.LOW);
            return dto;
        }

        dto.setSprintId(activeSprint.getId());

        // 2️⃣ Fetch sprint stories
        List<Story> sprintStories =
                storyRepository.findBySprintId(activeSprint.getId());

        List<Long> storyIds =
                sprintStories.stream()
                        .map(Story::getId)
                        .toList();

        // 3️⃣ Fetch sprint tasks (direct + via story)
        List<Task> directSprintTasks =
                taskRepository.findBySprintId(activeSprint.getId());

        Set<Long> taskIds = new HashSet<>();

        directSprintTasks.forEach(t -> taskIds.add(t.getId()));
        sprintStories.forEach(s ->
                s.getTasks().forEach(t -> taskIds.add(t.getId()))
        );

        // 4️⃣ Fetch relevant RiskLinks
        List<RiskLink> riskLinks =
                riskLinkRepository.findRelevantSprintRiskLinks(
                        activeSprint.getId(),
                        storyIds,
                        taskIds
                );

        if (riskLinks.isEmpty()) {
            dto.setRiskHealth(ProjectRiskSummaryDTO.RiskHealth.LOW);
            return dto;
        }

        // 5️⃣ Extract UNIQUE risks
        Map<Long, Risk> riskMap =
                riskLinks.stream()
                        .map(RiskLink::getRisk)
                        .collect(Collectors.toMap(
                                Risk::getId,
                                r -> r,
                                (a, b) -> a
                        ));

        List<Risk> risks = new ArrayList<>(riskMap.values());

        // 6️⃣ Fetch risk statuses
        List<RiskStatus> statuses =
                riskStatusRepository.findByProjectIdOrderBySortOrderAsc(projectId);

        Integer finalSortOrder =
                riskStatusRepository.findMaxSortOrder(projectId);

        Map<Long, RiskStatus> statusMap =
                statuses.stream()
                        .collect(Collectors.toMap(RiskStatus::getId, s -> s));

        // 7️⃣ Filter ACTIVE risks (not CLOSED)
        List<Risk> activeRisks =
                risks.stream()
                        .filter(r -> {
                            RiskStatus rs = statusMap.get(r.getStatusId());
                            return rs != null && rs.getSortOrder() < finalSortOrder;
                        })
                        .toList();

        dto.setTotalActiveRisks(activeRisks.size());

        // 8️⃣ Severity classification
        int high = 0, medium = 0, low = 0;
        int totalScore = 0;
        int maxScore = 0;

        for (Risk r : activeRisks) {
            if (r.getRiskScore() == null) continue;

            int score = r.getRiskScore();
            totalScore += score;
            maxScore = Math.max(maxScore, score);

            if (score >= 15) high++;
            else if (score >= 8) medium++;
            else low++;
        }

        dto.setHighRisks(high);
        dto.setMediumRisks(medium);
        dto.setLowRisks(low);
        dto.setTotalRiskScore(totalScore);
        dto.setMaxRiskScore(maxScore);

        // 9️⃣ Derive RiskHealth
        ProjectRiskSummaryDTO.RiskHealth health;

        if (high > 0 || maxScore >= 15) {
            health = ProjectRiskSummaryDTO.RiskHealth.HIGH;
        } else if (medium > 0) {
            health = ProjectRiskSummaryDTO.RiskHealth.MEDIUM;
        } else {
            health = ProjectRiskSummaryDTO.RiskHealth.LOW;
        }

        dto.setRiskHealth(health);

        return dto;
    }



    @Transactional(readOnly = true)
    public ProjectDto getProjectByKey(String projectKey) {
        Project project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new RuntimeException("Project not found with key: " + projectKey));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        long start = System.currentTimeMillis();
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<ProjectDto> dtos = projectRepository.findAll().stream()
                .map(project -> convertToDtoWithUsers(project, userMap))
                .collect(Collectors.toList());
        System.out.println("*****************Time taken to fetch all projects with users: " + (System.currentTimeMillis() - start) + " ms");
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<ProjectTimesheetDto> getAllTmsProjects() {
        long start = System.currentTimeMillis();
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<ProjectTimesheetDto> dtos = projectRepository.findAll().stream()
                .map(project -> convertToDtoWithUsers1(project, userMap))
                .collect(Collectors.toList());
        System.out.println("*****************Time taken to fetch all projects with users: " + (System.currentTimeMillis() - start) + " ms");
        return dtos;
    }

    /**
     * Convert with user objects already fetched and put into a map to avoid N+1
     */
    public ProjectDto convertToDtoWithUsers(Project project, Map<Long, UserDto> userMap) {
        long start = System.currentTimeMillis();
        ProjectDto dto = modelMapper.map(project, ProjectDto.class);
//        dto.setOwner(userMap.get(project.getOwnerId()));
//        dto.setMembers(
//                project.getMemberIds().stream()
//                        .map(userMap::get)
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList())
//        );
        System.out.println("###########################Time taken to convert single Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    public ProjectTimesheetDto convertToDtoWithUsers1(Project project, Map<Long, UserDto> userMap) {
        long start = System.currentTimeMillis();
        ProjectTimesheetDto dto = modelMapper.map(project, ProjectTimesheetDto.class);
        dto.setOwner(userMap.get(project.getOwnerId()));
        dto.setMembers(
                project.getMemberIds().stream()
                        .map(userMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        System.out.println("###########################Time taken to convert single Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ProjectPermissionDto> getAssociatedProjects(Long currentUserId) {

        // 1️⃣ Fetch all projects where user is OWNER or MEMBER
        List<Project> projects =
                projectRepository.findByOwnerIdOrMemberId(currentUserId);

        // 2️⃣ Build permission-aware response
        return projects.stream()
                .map(project -> buildPermissionDto(project, currentUserId))
                .filter(ProjectPermissionDto::isCanView)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ProjectTimesheetDto> getProjectsByOwner(Long ownerId) {

        List<Project> projects = projectRepository.findByOwnerId(ownerId);

        Set<Long> allUserIds = new HashSet<>();

        for (Project p : projects) {
            if (p.getOwnerId() != null) {
                allUserIds.add(p.getOwnerId());
            }
            if (p.getMemberIds() != null) {
                allUserIds.addAll(p.getMemberIds());
            }
        }

        Map<Long, UserDto> usersMap =
                userService.getUsersByIdsMap(allUserIds);

        return projects.stream()
                .map(project -> convertToDto2(project, usersMap))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveProjectsByOwner1(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .filter(project -> project.getStatus() == Project.ProjectStatus.ACTIVE)
                .map(project -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", project.getId());
                    map.put("name", project.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByMember(Long userId) {
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return projectRepository.findByMemberId(userId).stream()
                .map(project -> convertToDtoWithUsers(project, userMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update project (partial updates allowed).
     * Rules:
     * - Archived projects may only be changed back to ACTIVE (no other field updates allowed)
     * - Validate owner existence if ownerId provided
     * - Validate start/end dates (use existing values if partial)
     */
    public ProjectDto updateProject(Long id, ProjectDto updatedDto) {
        List<String> errors = new ArrayList<>();
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Project.ProjectStatus existingStatus = existing.getStatus();
        Project.ProjectStatus newStatus = updatedDto.getStatus();

        // If archived, only allow status -> ACTIVE (or no-op)
        if (existingStatus == Project.ProjectStatus.ARCHIVED) {
            if (newStatus == null) {
                errors.add("Archived project: to modify it you must set status to ACTIVE.");
            } else if (newStatus != Project.ProjectStatus.ACTIVE) {
                errors.add("Cannot update an archived project unless status is changed to ACTIVE.");
            }
        }

        // Determine effective start/end for validation (use existing when null)
        LocalDateTime effectiveStart = updatedDto.getStartDate() != null ? updatedDto.getStartDate() : existing.getStartDate();
        LocalDateTime effectiveEnd = updatedDto.getEndDate() != null ? updatedDto.getEndDate() : existing.getEndDate();

        if (effectiveStart != null && effectiveEnd != null && effectiveStart.isAfter(effectiveEnd)) {
            errors.add("Start date cannot be after end date.");
        }

        // Owner validation if provided
        if (updatedDto.getOwnerId() != null) {
            try {
                UserDto owner = userService.getUserWithRoles(updatedDto.getOwnerId());
                if (owner == null) {
                    errors.add("Owner not found with id: " + updatedDto.getOwnerId());
                }
            } catch (Exception e) {
                errors.add("Owner not found with id: " + updatedDto.getOwnerId());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // If archived -> active transition requested, only update status
        if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus == Project.ProjectStatus.ACTIVE) {
            existing.setStatus(Project.ProjectStatus.ACTIVE);
            Project saved = projectRepository.save(existing);
            return convertToDto(saved);
        }

        // Partial safe updates
        if (updatedDto.getName() != null) existing.setName(updatedDto.getName());
        if (updatedDto.getDescription() != null) existing.setDescription(updatedDto.getDescription());
        if (updatedDto.getProjectKey() != null) existing.setProjectKey(updatedDto.getProjectKey());
        if (updatedDto.getStartDate() != null) existing.setStartDate(updatedDto.getStartDate());
        if (updatedDto.getEndDate() != null) existing.setEndDate(updatedDto.getEndDate());
        if (updatedDto.getStatus() != null) existing.setStatus(updatedDto.getStatus());
        if (updatedDto.getCurrentStage() != null) existing.setCurrentStage(updatedDto.getCurrentStage());
        if (updatedDto.getOwnerId() != null) existing.setOwnerId(updatedDto.getOwnerId());

        if (updatedDto.getMemberIds() != null) {
            existing.setMemberIds(new HashSet<>(updatedDto.getMemberIds()));
        }

        Project saved = projectRepository.save(existing);
        return convertToDto(saved);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        statusRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    public ProjectDto addMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        if (!project.getMemberIds().contains(userId)) {
            project.getMemberIds().add(userId);
            projectRepository.save(project);
        }

        return convertToDto(project);
    }

    public ProjectDto removeMemberFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.getMemberIds().remove(userId);
        projectRepository.save(project);

        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(String name, Project.ProjectStatus status, Pageable pageable) {
        if (name != null && status != null) {
            return projectRepository.findByNameContainingAndStatus(name, status, pageable)
                    .map(this::convertToDto);
        } else if (name != null) {
            return projectRepository.findByNameContaining(name, pageable)
                    .map(this::convertToDto);
        } else if (status != null) {
            return projectRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return projectRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }

    /**
     * Convert Project entity to ProjectDto including owner and member user info
     */
    public ProjectDto convertToDto(Project project) {
        long start = System.currentTimeMillis();
        ProjectDto dto = ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .projectKey(project.getProjectKey())
                .description(project.getDescription())
                .status(project.getStatus())
                .currentStage(project.getCurrentStage())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .clientId(project.getClientId())
                .rmId(project.getRmId())
                .deliveryOwnerId(project.getDeliveryOwnerId())
                .deliveryModel(project.getDeliveryModel())
                .primaryLocation(project.getPrimaryLocation())
                .riskLevel(project.getRiskLevel())
                .riskLevelUpdatedAt(project.getRiskLevelUpdatedAt())
                .priorityLevel(project.getPriorityLevel())  
                .projectBudget(project.getProjectBudget())
                .projectBudgetCurrency(project.getProjectBudgetCurrency())
                
                .build();

        dto.setOwnerId(project.getOwnerId());
//        dto.setOwner(project.getOwnerId() != null ? safeGetUserWithRoles(project.getOwnerId()) : null);

        if (project.getMemberIds() != null) {
            Set<Long> memberIds = project.getMemberIds();
            dto.setMemberIds(memberIds);
//            dto.setMembers(userService.getUsersByIds(new ArrayList<>(memberIds)));
        }

        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&Time taken to convert Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    public ProjectTimesheetDto convertToDto3(Project project) {
        long start = System.currentTimeMillis();
        ProjectTimesheetDto dto = ProjectTimesheetDto.builder()
                .id(project.getId())
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();

//        dto.setOwnerId(project.getOwnerId());
        dto.setOwner(project.getOwnerId() != null ? safeGetUserWithRoles(project.getOwnerId()) : null);

        if (project.getMemberIds() != null) {
            Set<Long> memberIds = project.getMemberIds();
//            dto.setMemberIds(memberIds);
            dto.setMembers(userService.getUsersByIds(new ArrayList<>(memberIds)));
        }

        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&Time taken to convert Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    private ProjectTimesheetDto convertToDto2(
            Project project,
            Map<Long, UserDto> usersMap) {

        ProjectTimesheetDto dto = ProjectTimesheetDto.builder()
                .id(project.getId())
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();

        dto.setOwner(usersMap.get(project.getOwnerId()));

        if (project.getMemberIds() != null) {
            dto.setMembers(
                    project.getMemberIds().stream()
                            .map(usersMap::get)
                            .toList()
            );
        }

        return dto;
    }


    private UserDto safeGetUserWithRoles(Long ownerId) {
        try {
            return userService.getUserWithRoles(ownerId);
        } catch (Exception e) {
            return null;
        }
    }

    public ProjectDto unarchiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getStatus() != Project.ProjectStatus.ARCHIVED) {
            throw new RuntimeException("Only archived projects can be unarchived.");
        }

        project.setStatus(Project.ProjectStatus.ACTIVE);
        Project updated = projectRepository.save(project);
        return convertToDto(updated);
    }

    public List<ProjectTasksDto> getAllProjectsWithTasks() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(project -> {
            List<ProjectTasksDto.TaskDto> taskDtos = project.getTasks().stream()
                    .map(task -> new ProjectTasksDto.TaskDto(task.getId(), task.getTitle()))
                    .collect(Collectors.toList());
            return new ProjectTasksDto(project.getId(), project.getName(), taskDtos);
        }).collect(Collectors.toList());
    }

    public List<ProjectIdName> getAllProjectInfo() {
        return projectRepository.findAll().stream()
                .filter(p -> p.getStatus().equals(Project.ProjectStatus.ACTIVE))
                .map(p -> {
                    ProjectIdName pro = new ProjectIdName();
                    pro.setId(p.getId());
                    pro.setName(p.getName());
                    return pro;
                })
                .collect(Collectors.toList());
    }

    public List<UserDto> getProjectMembers(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return userService.getUsersByIds(new ArrayList<>(project.getMemberIds()));
    }

    public List<UserDto> getProjectMembersOwner(Long id) {
        // duplicate of getProjectMembers - preserved for compatibility with your original API
        return getProjectMembers(id);
    }

    public List<ProjectIdName> getActiveProjectsByMember(Long userId) {
        return projectRepository.findByMemberIdsAndStatus(userId, Project.ProjectStatus.ACTIVE).stream()
                .map(project -> {
                    ProjectIdName pro = new ProjectIdName();
                    pro.setId(project.getId());
                    pro.setName(project.getName());
                    return pro;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getProjectOwner(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        Long ownerId = project.getOwnerId();
        if (ownerId == null) {
            throw new RuntimeException("Project has no assigned owner.");
        }
        return userService.getUserWithRoles(ownerId);
    }

    public List<ProjectSummary> getProjectSummariesByOwner(Long ownerId) {
        return projectRepository.findProjectSummariesByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummary> getAccessibleProjects(Long userId) {
        return projectRepository.findAccessibleProjectSummaries(userId);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByOwner(Long ownerId, String period) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth ym = YearMonth.parse(period, formatter);

        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        return projectRepository.findActiveProjectsByPeriod(ownerId, monthStart, monthEnd)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProjectPermissionDto buildPermissionDto(Project project, Long currentUserId) {

        // 1️⃣ Is current user the OWNER?
        boolean isOwner = project.getOwnerId().equals(currentUserId);

        // 2️⃣ Is current user a MEMBER?
        boolean isMember = project.getMemberIds() != null &&
                project.getMemberIds().contains(currentUserId);

        // 3️⃣ Permission rules
        boolean canView = isOwner || isMember;
        boolean canEdit = isOwner;
        boolean canDelete = isOwner;

        // 4️⃣ Build final response
        return ProjectPermissionDto.builder()
                .project(convertToDto(project)) // your existing mapper
                .canView(canView)
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }


}
