package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEditDto {

    private Long id;
    private String name;
    private String projectKey;
    private String description;

    private Project.ProjectStatus status;
    private Project.ProjectStage currentStage;
    private Project.DeliveryModel deliveryModel;
    private String primaryLocation;

    private Project.RiskLevel riskLevel;
    private Project.PriorityLevel priorityLevel;

    private BigDecimal projectBudget;
    private String projectBudgetCurrency;

    private UUID clientId;
    private Long ownerId;
    private Long rmId;
    private Long deliveryOwnerId;

    private Set<Long> memberIds;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}