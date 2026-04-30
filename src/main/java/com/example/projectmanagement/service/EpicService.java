package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.dto.*;
//import com.example.projectmanagement.entity.Epic.EpicStatus;
import com.example.projectmanagement.entity.Epic.Priority;
import com.example.projectmanagement.repository.*;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EpicService {

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private RiskLinkRepository riskLinkRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private MitigationPlanRepository mitigationPlanRepository;

    @Autowired
    private RiskAttachmentRepository riskAttachmentRepository;







    // ✅ Create Epic
    public EpicDto createEpic(EpicDto epicDto, Long userId) {

        Epic epic = convertToEntity(epicDto);
        epic.setCreatedBy(userId);

        boolean exists = epicRepository.existsByNameAndProjectId(epic.getName(), epic.getProject().getId());
        if (exists) {
            throw new IllegalArgumentException("Epic name already exists in this project");
        }

        // Set dynamic status
        if (epicDto.getStatusId() != null) {
            Status status = statusRepository.findById(epicDto.getStatusId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid status"));
            epic.setStatus(status);
        }

        Epic savedEpic = epicRepository.save(epic);
        return convertToDto(savedEpic);
    }

    // ✅ Get All Epics
    public List<EpicDto> getAllEpics() {
        return epicRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get by ID
    public EpicDto getEpicById(Long id) {
        Optional<Epic> optionalEpic = epicRepository.findById(id);
        return optionalEpic.map(this::convertToDto).orElse(null);
    }

    // ✅ Update Epic
    public EpicDto updateEpic(Long id, EpicDto epicDto) {
        Optional<Epic> optionalEpic = epicRepository.findById(id);
        if (optionalEpic.isPresent()) {
            Epic existingEpic = optionalEpic.get();

            existingEpic.setName(epicDto.getName());
            existingEpic.setDescription(epicDto.getDescription());

            // ✅ Convert String to Enum
            if (epicDto.getStatusId() != null) {
                Status status = statusRepository.findById(epicDto.getStatusId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid status"));
                existingEpic.setStatus(status);
            }

            if (epicDto.getPriority() != null) {
                existingEpic.setPriority(Epic.Priority.valueOf(epicDto.getPriority()));
            }

            existingEpic.setProgressPercentage(epicDto.getProgressPercentage());

            // ✅ Convert LocalDateTime to LocalDate
            if (epicDto.getDueDate() != null) {
                existingEpic.setDueDate(epicDto.getDueDate().toLocalDate());
            }

            if (epicDto.getProjectId() != null) {
                Project project = projectRepository.findById(epicDto.getProjectId()).orElse(null);
                existingEpic.setProject(project);
            }

            

            Epic updatedEpic = epicRepository.save(existingEpic);
            return convertToDto(updatedEpic);
        }
        return null;
    }


    // ✅ Delete Epic
    @Transactional
    public boolean deleteEpic(Long id) {

        Epic epic = epicRepository.findById(id)
                .orElse(null);

        if (epic == null) {
            return false;
        }

        /*
         * DEMO BEHAVIOR:
         * Currently, when an Epic is deleted, all Risks linked to this Epic
         * are also deleted along with their child records:
         * RiskLink, MitigationPlan, and RiskAttachment.
         *
         * TODO:
         * In production, do not directly hard-delete Risks here.
         * Instead:
         * 1. Ask user whether to delete risks or only unlink them
         * 2. Prefer soft delete for Risk records
         * 3. If the Risk is linked to other Story/Task/Sprint items,
         *    only delete this RiskLink and keep the Risk
         * 4. Preserve mitigation plans and attachments for audit/history if needed
         */
        List<RiskLink> riskLinks = riskLinkRepository.findByLinkedTypeAndLinkedId(
                RiskLink.LinkedType.Epic,
                id
        );

        if (!riskLinks.isEmpty()) {

            List<Risk> risksToDelete = riskLinks.stream()
                    .map(RiskLink::getRisk)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            // 1. Delete risk links first
            riskLinkRepository.deleteAll(riskLinks);

            // 2. Delete child records of risks before deleting risks
            mitigationPlanRepository.deleteByRiskIn(risksToDelete);
            riskAttachmentRepository.deleteByRiskIn(risksToDelete);

            // 3. Delete associated risks
            riskRepository.deleteAll(risksToDelete);
        }

        epicRepository.delete(epic);

        return true;
    }

    // ✅ DTO Conversion
    private EpicDto convertToDto(Epic epic) {
        EpicDto dto = new EpicDto();
        dto.setId(epic.getId());
        dto.setName(epic.getName());
        dto.setDescription(epic.getDescription());
        dto.setStartDate(epic.getStartDate());
        dto.setCreatedBy(epic.getCreatedBy());
        // Convert enum to String
        if (epic.getStatus() != null) {
            dto.setStatusId(epic.getStatus().getId());
            dto.setStatusName(epic.getStatus().getName());
        }


        if (epic.getPriority() != null) {
            dto.setPriority(epic.getPriority().name()); // Priority to String
        }

        dto.setProgressPercentage(epic.getProgressPercentage());

        // Convert LocalDate to LocalDateTime for DTO
        if (epic.getDueDate() != null) {
            dto.setDueDate(epic.getDueDate().atStartOfDay()); // LocalDate -> LocalDateTime
        }

        if (epic.getProject() != null) {
            dto.setProjectId(epic.getProject().getId());
        }

        

        return dto;
    }

    private Epic convertToEntity(EpicDto dto) {
        Epic epic = new Epic();
        epic.setName(dto.getName());
        epic.setDescription(dto.getDescription());
        epic.setStartDate(dto.getStartDate());

        // Convert String to Enum safely
        if (epic.getStatus() != null) {
            dto.setStatusId(epic.getStatus().getId());
            dto.setStatusName(epic.getStatus().getName());
        }


        if (dto.getPriority() != null) {
            epic.setPriority(Epic.Priority.valueOf(dto.getPriority().toUpperCase()));
        }

        epic.setProgressPercentage(dto.getProgressPercentage());

        // Convert LocalDateTime to LocalDate
        if (dto.getDueDate() != null) {
            epic.setDueDate(dto.getDueDate().toLocalDate());
        }

        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId()).orElse(null);
            epic.setProject(project);
        }

        

        return epic;
    }


    // ✅ Optional methods to implement
    public List<EpicDto> getEpicsByProjectId(Long projectId) {
        return epicRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<EpicDto> getEpicsByOrganizationId(Long organizationId) {
        // Assuming you have org info in project or epics
        throw new UnsupportedOperationException("Unimplemented method 'getEpicsByOrganizationId'");
    }

    public List<EpicDto> getEpicsByStatus(Long statusId) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid status ID"));

        return epicRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public Page<EpicDto> searchEpics(String name, Priority priority, Long projectId, Pageable pageable) {
        throw new UnsupportedOperationException("Unimplemented method 'searchEpics'");
    }

}
