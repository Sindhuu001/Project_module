package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProjectDto {

    /* =====================
       BASIC IDENTIFIERS
       ===================== */
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    private String projectKey;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /* =====================
       STATUS & STAGE
       ===================== */
    private Project.ProjectStatus status;
    private Project.ProjectStage currentStage;

    /* =====================
       OWNERSHIP & IDENTITY
       ===================== */
    @NotNull(message = "Client ID is required")
    private String clientId;

    @NotNull(message = "Owner is required")
    private Long ownerId;

    private Long rmId;
    private Long deliveryOwnerId;

    /* =====================
       DELIVERY / RISK / PRIORITY
       ===================== */
    private Project.DeliveryModel deliveryModel;
    private String primaryLocation;
    private Project.RiskLevel riskLevel;
    private LocalDateTime riskLevelUpdatedAt;
    private Project.PriorityLevel priorityLevel;

    /* =====================
       BUDGET
       ===================== */
    private BigDecimal projectBudget;
    private String projectBudgetCurrency;

    /* =====================
       MEMBERS
       ===================== */
    private Set<Long> memberIds;

    /* =====================
       DATES & AUDIT
       ===================== */
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* =====================
       CONVENIENCE CONSTRUCTOR
       ===================== */
    public ProjectDto(String name, String projectKey, String description, String clientId, Long ownerId) {
        this.name = name;
        this.projectKey = projectKey;
        this.description = description;
        this.clientId = clientId;
        this.ownerId = ownerId;
    }
}
