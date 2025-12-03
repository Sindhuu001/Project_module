package com.example.projectmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RiskStatusCreateRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;
}
