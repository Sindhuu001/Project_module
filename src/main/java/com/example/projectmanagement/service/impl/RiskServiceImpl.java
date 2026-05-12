package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.service.RiskService;
import com.example.projectmanagement.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RiskServiceImpl implements RiskService {

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RiskCategoryRepository categoryRepository;

    @Autowired
    private RiskStatusRepository statusRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RiskLinkRepository riskLinkRepository;

    @Autowired
    private MitigationPlanRepository mitigationPlanRepository;

    @Autowired
    private RiskAttachmentRepository riskAttachmentRepository;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public RiskResponse createRisk(RiskRequest request, Long createdBy) {

        validateRequest(request);
        checkDuplicate(request);

        Risk risk = new Risk();

        risk.setProjectId(request.getProjectId());
        risk.setOwnerId(request.getOwnerId());
        risk.setCreatedBy(createdBy);
        risk.setReporterId(request.getReporterId());
        risk.setCategoryId(request.getCategoryId());
        risk.setStatusId(request.getStatusId());

        risk.setTitle(request.getTitle());
        risk.setDescription(request.getDescription());

        risk.setProbability(request.getProbability());
        risk.setImpact(request.getImpact());

        risk.setRiskScore(request.getProbability() * request.getImpact());

        risk.setTriggers(request.getTriggers());
        risk.setCreatedAt(LocalDateTime.now());

        return toResponse(riskRepository.save(risk));
    }

    // =========================================================
    // GET ALL BY PROJECT
    // =========================================================

    @Override
    public List<RiskResponse> getRisksByProject(Long projectId) {

        return riskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public RiskResponse getRiskById(Long id) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Risk not found with ID: " + id));

        return toResponse(risk);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public RiskResponse updateRisk(Long id, RiskRequest request) {

        validateRequest(request);

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Risk not found with ID: " + id));

        if (riskRepository.existsByProjectIdAndTitleAndCategoryIdAndIdNot(
                request.getProjectId(),
                request.getTitle(),
                request.getCategoryId(),
                id
        )) {
            throw new IllegalArgumentException(
                    "A risk with the same title and category already exists in this project."
            );
        }

        risk.setProjectId(request.getProjectId());
        risk.setOwnerId(request.getOwnerId());
        risk.setReporterId(request.getReporterId());

        risk.setCategoryId(request.getCategoryId());
        risk.setStatusId(request.getStatusId());

        risk.setTitle(request.getTitle());
        risk.setDescription(request.getDescription());

        risk.setProbability(request.getProbability());
        risk.setImpact(request.getImpact());

        risk.setRiskScore(request.getProbability() * request.getImpact());

        risk.setTriggers(request.getTriggers());

        risk.setUpdatedAt(LocalDateTime.now());

        return toResponse(riskRepository.save(risk));
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @Transactional
    public void deleteRisk(Long id) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Risk not found with ID: " + id));

        List<RiskLink> riskLinks = riskLinkRepository.findByRisk(risk);

        if (!riskLinks.isEmpty()) {
            riskLinkRepository.deleteAll(riskLinks);
        }

        mitigationPlanRepository.deleteByRiskIn(List.of(risk));

        riskAttachmentRepository.deleteByRiskIn(List.of(risk));

        riskRepository.delete(risk);
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Override
    public RiskResponse updateStatus(Long id, RiskStatusUpdateRequest request) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Risk not found with ID: " + id));

        statusRepository.findById(request.getStatusId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Status not found with ID: " + request.getStatusId()
                        ));

        risk.setStatusId(request.getStatusId());

        if (isClosedStatus(request.getStatusId())) {
            risk.setClosedAt(LocalDateTime.now());
        } else {
            risk.setClosedAt(null);
        }

        risk.setUpdatedAt(LocalDateTime.now());

        return toResponse(riskRepository.save(risk));
    }

    // =========================================================
    // PAGINATED RISKS
    // =========================================================

    @Override
    public RiskResponseDTO getRisksWithPagination(
            Long projectId,
            RiskLink.LinkedType linkedType,
            Long linkedId,
            int page,
            int size,
            String severityFilter
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("id").descending()
        );

        Page<Risk> risksPage;

        if (linkedType == null || linkedId == null) {

            risksPage = riskRepository.findAllLinkedRisks(
                    projectId,
                    pageable
            );

        } else {

            risksPage =
                    riskRepository.findByRiskLinks_LinkedTypeAndRiskLinks_LinkedId(
                            linkedType,
                            linkedId,
                            pageable
                    );
        }

        List<RiskItemDTO> items = risksPage.getContent()
                .stream()
                .map(risk -> mapToRiskItemDTO(risk, severityFilter))
                .filter(Objects::nonNull)
                .toList();

        return buildRiskResponseDTO(
                linkedId,
                page,
                size,
                risksPage,
                items
        );
    }

    // =========================================================
    // SEARCH + PAGINATION
    // =========================================================

    @Override
    public RiskResponseDTO getLinkedRisks(
            Long projectId,
            RiskLink.LinkedType linkedType,
            Long linkedId,
            int page,
            int size,
            String search
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("id").descending()
        );

        Page<Risk> risksPage =
                riskRepository.findLinkedRisksWithSearch(
                        projectId,
                        linkedType,
                        linkedId,
                        (search == null || search.isBlank())
                                ? null
                                : search.trim(),
                        pageable
                );

        List<RiskItemDTO> items = risksPage.getContent()
                .stream()
                .map(risk -> mapToRiskItemDTO(risk, null))
                .filter(Objects::nonNull)
                .toList();

        return buildRiskResponseDTO(
                linkedId,
                page,
                size,
                risksPage,
                items
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private RiskResponseDTO buildRiskResponseDTO(
            Long linkedId,
            int page,
            int size,
            Page<Risk> risksPage,
            List<RiskItemDTO> items
    ) {

        RiskSummaryDTO summary = new RiskSummaryDTO(
                (int) risksPage.getTotalElements(),

                (int) items.stream()
                        .filter(i ->
                                "High".equalsIgnoreCase(i.getSeverity()))
                        .count(),

                items.stream()
                        .mapToInt(RiskItemDTO::getRiskScore)
                        .average()
                        .orElse(0.0)
        );

        PaginationDTO pagination = new PaginationDTO(
                page,
                size,
                risksPage.getTotalPages(),
                risksPage.getTotalElements()
        );

        return new RiskResponseDTO(
                linkedId,
                summary,
                pagination,
                items
        );
    }

    private RiskItemDTO mapToRiskItemDTO(
            Risk risk,
            String severityFilter
    ) {

        int riskScore = risk.getProbability() * risk.getImpact();

        String severity = calculateSeverity(riskScore);

        if (severityFilter != null &&
                !severity.equalsIgnoreCase(severityFilter)) {
            return null;
        }

        String status = statusRepository.findById(risk.getStatusId())
                .map(s -> s.getName())
                .orElse("Unknown");

        String owner =
                risk.getOwnerId() == null
                        ? "Unknown"
                        : userService.getUserById(risk.getOwnerId()).getName();

        String reporter =
                risk.getReporterId() == null
                        ? "Unknown"
                        : userService.getUserById(risk.getReporterId()).getName();

        return new RiskItemDTO(
                risk.getId(),
                risk.getTitle(),
                risk.getProbability(),
                risk.getImpact(),
                riskScore,
                severity,
                status,
                owner,
                reporter,
                risk.getOwnerId(),
                risk.getReporterId()
        );
    }

    private String calculateSeverity(int score) {

        if (score <= 6) return "Low";

        if (score <= 14) return "Medium";

        return "High";
    }

    private void validateRequest(RiskRequest request) {

        projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found with ID: "
                                        + request.getProjectId()
                        ));

        if (request.getCategoryId() != null) {

            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Category not found with ID: "
                                            + request.getCategoryId()
                            ));
        }

        statusRepository.findById(request.getStatusId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Status not found with ID: "
                                        + request.getStatusId()
                        ));
    }

    private void checkDuplicate(RiskRequest request) {

        boolean exists =
                riskRepository.existsByProjectIdAndTitleAndCategoryId(
                        request.getProjectId(),
                        request.getTitle(),
                        request.getCategoryId()
                );

        if (exists) {

            throw new IllegalArgumentException(
                    "A risk with the same title and category already exists in this project."
            );
        }
    }

    private boolean isClosedStatus(Long statusId) {

        return statusRepository.findById(statusId)
                .map(status ->
                        status.getSortOrder() != null &&
                                isLastSortOrder(status.getSortOrder()))
                .orElse(false);
    }

    private boolean isLastSortOrder(Integer sortOrder) {

        Integer maxSortOrder = statusRepository.findAll()
                .stream()
                .map(s -> s.getSortOrder())
                .max(Integer::compareTo)
                .orElse(Integer.MIN_VALUE);

        return sortOrder.equals(maxSortOrder);
    }

    private RiskResponse toResponse(Risk risk) {

        RiskResponse response = new RiskResponse();

        response.setId(risk.getId());

        response.setProjectId(risk.getProjectId());

        response.setOwnerId(risk.getOwnerId());

        response.setReporterId(risk.getReporterId());

        response.setCategoryId(risk.getCategoryId());

        response.setStatusId(risk.getStatusId());

        response.setTitle(risk.getTitle());

        response.setDescription(risk.getDescription());

        response.setProbability(risk.getProbability());

        response.setImpact(risk.getImpact());

        response.setRiskScore(risk.getRiskScore());

        response.setTriggers(risk.getTriggers());

        response.setCreatedAt(risk.getCreatedAt());

        response.setUpdatedAt(risk.getUpdatedAt());

        response.setClosedAt(risk.getClosedAt());

        return response;
    }
}