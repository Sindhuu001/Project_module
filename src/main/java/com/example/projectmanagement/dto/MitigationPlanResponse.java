package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MitigationPlanResponse {
    private Long id;
    private Long riskId;
    private String mitigation;
    private String contingency;
    private Long ownerId;
    private LocalDateTime lastReviewedAt;

    // New fields
    private Boolean used;       // Whether the mitigation has been applied
    private Boolean effective;  // Whether it was effective
    private String notes;       // Additional observations/comments
}
