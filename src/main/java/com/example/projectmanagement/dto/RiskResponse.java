package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskResponse {
    private Long id;
    private Long projectId;
    private Long ownerId;
    private Long reporterId;
    private Long categoryId;
    private Long statusId;
    private String title;
    private String description;
    private Byte probability;
    private Byte impact;
    private Integer riskScore;
    private String triggers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
