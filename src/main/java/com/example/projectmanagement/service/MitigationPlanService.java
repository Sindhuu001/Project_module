package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.MitigationPlanRequest;
import com.example.projectmanagement.dto.MitigationPlanResponse;
import com.example.projectmanagement.dto.MitigationPlanStatusPatchRequest;

import java.util.List;

public interface MitigationPlanService {

    MitigationPlanResponse createPlan(MitigationPlanRequest request);


    MitigationPlanResponse updatePlan(Long id, MitigationPlanRequest request);

    void deletePlan(Long id);

    List<MitigationPlanResponse> getPlansByRiskId(Long riskId);

    MitigationPlanResponse updateStatus(Long id, MitigationPlanStatusPatchRequest request);

}
