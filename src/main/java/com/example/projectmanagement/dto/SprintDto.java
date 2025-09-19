package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Sprint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SprintDto {
    
    private Long id;
    
    @NotBlank(message = "Sprint name is required")
    @Size(min = 2, max = 100, message = "Sprint name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Goal cannot exceed 500 characters")
    private String goal;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    private Sprint.SprintStatus status;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private ProjectDto project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public SprintDto() {}
    
    public SprintDto(String name, String goal, LocalDateTime startDate, LocalDateTime endDate, Long projectId) {
        this.name = name;
        this.goal = goal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectId = projectId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public Sprint.SprintStatus getStatus() { return status; }
    public void setStatus(Sprint.SprintStatus status) { this.status = status; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public ProjectDto getProject() { return project; }
    public void setProject(ProjectDto project) { this.project = project; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}