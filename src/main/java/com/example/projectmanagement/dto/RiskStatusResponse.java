package com.example.projectmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiskStatusResponse {

    private Long id;
    private Long projectId;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
