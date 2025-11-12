package com.example.projectmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusDto {

    private Long statusId;

    @NotBlank(message = "Status name is required")
    @Size(min = 2, max = 100, message = "Status name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;

    private Boolean isBug;

    private Boolean isPredefined;

    private Boolean isActive;

    // Only include the project ID, not the full Project object
    @NotNull(message = "Project ID is required")
    private Long projectId;

    // Optional: if you want to embed minimal project info
    private String projectName;
}
