package com.example.projectmanagement.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class EpicDto {
    private Long id;
    @NotBlank(message = "Epic name is required")
    private String name;
    @Size(max = 1000, message = "Description can’t exceed 1000 characters")
    private String description;
    private Long statusId;
    private String statusName;
    private String priority;
    private Integer progressPercentage;
    private LocalDateTime dueDate;
    @NotNull(message = "Project ID is required")
    private Long projectId;
}
