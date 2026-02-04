package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Project;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRiskUpdateDto {

    @NotNull(message = "Risk level is required")
    private Project.RiskLevel riskLevel;
}
