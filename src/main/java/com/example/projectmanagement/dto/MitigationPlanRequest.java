package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class MitigationPlanRequest {
    private Long riskId;
    private String mitigation;
    private String contingency;
    private Long ownerId;

    // New fields
    private Boolean used;       // Has the mitigation been applied
    private Boolean effective;  // Was it effective
    private String notes;       // Any observations or comments
}
