package com.example.projectmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiskCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
