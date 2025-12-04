package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.config.RiskConfig;
import com.example.projectmanagement.dto.RiskStatusCreateRequest;
import com.example.projectmanagement.dto.RiskStatusResponse;
import com.example.projectmanagement.entity.RiskStatus;
import com.example.projectmanagement.repository.RiskStatusRepository;
import com.example.projectmanagement.service.RiskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service

public class RiskStatusServiceImpl implements RiskStatusService {

    @Autowired
    private RiskStatusRepository repository;

    @Autowired
    private RiskStatusRepository riskStatusRepository;

    @Autowired
    private RiskConfig riskConfig;


    public void createDefaultStatusesForProject(Long projectId) {

        int order = 1;

        for (String statusName : riskConfig.getDefaultStatuses()) {

            RiskStatus status = new RiskStatus();
            status.setProjectId(projectId);
            status.setName(statusName);
            status.setSortOrder(order++);

            riskStatusRepository.save(status);
        }
    }

    // ---------------------- MAPPERS ----------------------
    private RiskStatusResponse toResponse(RiskStatus entity) {
        RiskStatusResponse res = new RiskStatusResponse();
        res.setId(entity.getId());
        res.setProjectId(entity.getProjectId());
        res.setName(entity.getName());
        res.setSortOrder(entity.getSortOrder());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }

    private RiskStatus toEntity(RiskStatusCreateRequest req) {
        RiskStatus entity = new RiskStatus();
        entity.setProjectId(req.getProjectId());
        entity.setName(req.getName());
        entity.setSortOrder(req.getSortOrder());
        return entity;
    }
    // -----------------------------------------------------

    @Override
    public RiskStatusResponse createRiskStatus(RiskStatusCreateRequest request) {

        boolean exists = repository.existsByProjectIdAndName(request.getProjectId(), request.getName());
        if (exists) {
            throw new RuntimeException("Risk status already exists for this project: " + request.getName());
        }

        RiskStatus status = toEntity(request);
        RiskStatus saved = repository.save(status);

        return toResponse(saved);
    }

    @Override
    public List<RiskStatusResponse> getRiskStatusesByProject(Long projectId) {
        return repository.findByProjectIdOrderBySortOrder(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RiskStatusResponse updateRiskStatus(Long id, RiskStatusCreateRequest request) {
        RiskStatus existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk status not found: " + id));

        // prevent duplicate names in same project
        boolean exists = repository.existsByProjectIdAndName(request.getProjectId(), request.getName());
        if (exists && !existing.getName().equals(request.getName())) {
            throw new RuntimeException("Risk status with same name already exists");
        }

        existing.setName(request.getName());
        existing.setSortOrder(request.getSortOrder());
        existing.setProjectId(request.getProjectId());

        RiskStatus saved = repository.save(existing);
        return toResponse(saved);
    }

    @Override
    public void deleteRiskStatus(Long id, Long newStatusId) {
        if (newStatusId != null && id.equals(newStatusId)) {
            throw new RuntimeException("New status cannot be same as deleted status.");
        }

        repository.deleteById(id);
    }

    @Override
    public List<RiskStatusResponse> reorderRiskStatuses(Map<Long, Integer> newOrder) {
        List<RiskStatus> statuses = repository.findAllById(newOrder.keySet());

        statuses.forEach(status ->
                status.setSortOrder(newOrder.get(status.getId()))
        );

        repository.saveAll(statuses);

        return statuses.stream()
                .sorted(Comparator.comparingInt(RiskStatus::getSortOrder))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RiskStatus getRiskStatus(Long id) {
        return riskStatusRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Risk status not found with id " + id)
                );
    }

}
