package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDto {

    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    private String projectKey;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Project.ProjectStatus status;

    @NotNull(message = "Owner is required")
    private Long ownerId;

    // ✅ Add this field to match the frontend's payload
    private List<Long> memberIds;

    private UserDto owner;
    private List<UserDto> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ProjectDto() {}

    public ProjectDto(String name, String projectKey, String description, Long ownerId) {
        this.name = name;
        this.projectKey = projectKey;
        this.description = description;
        this.ownerId = ownerId;
    }

    
}
