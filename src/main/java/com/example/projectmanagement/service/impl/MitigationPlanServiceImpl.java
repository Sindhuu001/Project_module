package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.MitigationPlanRequest;
import com.example.projectmanagement.dto.MitigationPlanResponse;
import com.example.projectmanagement.dto.MitigationPlanStatusPatchRequest;
import com.example.projectmanagement.entity.MitigationPlan;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.repository.MitigationPlanRepository;
import com.example.projectmanagement.repository.RiskRepository;
import com.example.projectmanagement.service.MitigationPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MitigationPlanServiceImpl implements MitigationPlanService {

    @Autowired
    private MitigationPlanRepository planRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Override
    public MitigationPlanResponse createPlan(MitigationPlanRequest request) {
        Risk risk = riskRepository.findById(request.getRiskId())
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        boolean exists = planRepository.existsByRiskAndMitigation(risk, request.getMitigation());
        if (exists) {
            throw new IllegalArgumentException("A mitigation plan with the same description already exists for this risk");
        }

        MitigationPlan plan = new MitigationPlan();
        plan.setRisk(risk);
        plan.setMitigation(request.getMitigation());
        plan.setContingency(request.getContingency());
        plan.setOwnerId(request.getOwnerId());
        plan.setUsed(request.getUsed() != null ? request.getUsed() : false);
        plan.setEffective(request.getEffective());
        plan.setNotes(request.getNotes());
        plan.setLastReviewedAt(LocalDateTime.now());

        return toResponse(planRepository.save(plan));
    }

    @Override
    public List<MitigationPlanResponse> getPlansByRiskId(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        List<MitigationPlan> plans = planRepository.findByRisk(risk);

        // ✅ RETURN EMPTY LIST (NOT ERROR)
        return plans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public MitigationPlanResponse updateStatus(Long id, MitigationPlanStatusPatchRequest request) {
        MitigationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mitigation plan not found"));

        if (request.getUsed() != null) plan.setUsed(request.getUsed());
        if (request.getEffective() != null) plan.setEffective(request.getEffective());

        plan.setLastReviewedAt(LocalDateTime.now());

        return toResponse(planRepository.save(plan));
    }


    @Override
    public MitigationPlanResponse updatePlan(Long id, MitigationPlanRequest request) {
        MitigationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mitigation plan not found"));

        Risk risk = riskRepository.findById(request.getRiskId())
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        plan.setRisk(risk);
        plan.setMitigation(request.getMitigation());
        plan.setContingency(request.getContingency());
        plan.setOwnerId(request.getOwnerId());
        plan.setUsed(request.getUsed() != null ? request.getUsed() : plan.getUsed());
        plan.setEffective(request.getEffective());
        plan.setNotes(request.getNotes());
        plan.setLastReviewedAt(LocalDateTime.now());

        return toResponse(planRepository.save(plan));
    }

    @Override
    public void deletePlan(Long id) {
        MitigationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mitigation plan not found"));
        planRepository.delete(plan);
    }

    private MitigationPlanResponse toResponse(MitigationPlan plan) {
        MitigationPlanResponse response = new MitigationPlanResponse();
        response.setId(plan.getId());
        response.setRiskId(plan.getRisk().getId());
        response.setMitigation(plan.getMitigation());
        response.setContingency(plan.getContingency());
        response.setOwnerId(plan.getOwnerId());
        response.setUsed(plan.getUsed());
        response.setEffective(plan.getEffective());
        response.setNotes(plan.getNotes());
        response.setLastReviewedAt(plan.getLastReviewedAt());
        return response;
    }
}
