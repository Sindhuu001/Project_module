package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Story;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class StoryDto {
    
    private Long id;
    
    @NotBlank(message = "Story title is required")
    @Size(min = 2, max = 200, message = "Story title must be between 2 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private Story.StoryStatus status;
    private Story.Priority priority;
    private Integer storyPoints;
    
    @Size(max = 2000, message = "Acceptance criteria cannot exceed 2000 characters")
    private String acceptanceCriteria;
    
    
    private Long epicId;
    
    @NotNull(message = "Reporter ID is required")
    private Long reporterId;

    
    private Long sprintId;
 
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private Long assigneeId;
    private UserDto assignee;
    private UserDto reporter;
    private EpicDto epic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public StoryDto() {}
    
    public StoryDto(String title, String description, Long epicId, Long reporterId) {
        this.title = title;
        this.description = description;
        this.epicId = epicId;
        this.reporterId = reporterId;
    }
    public StoryDto(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Story.StoryStatus getStatus() { return status; }
    public void setStatus(Story.StoryStatus status) { this.status = status; }
    
    public Story.Priority getPriority() { return priority; }
    public void setPriority(Story.Priority priority) { this.priority = priority; }
    
    public Integer getStoryPoints() { return storyPoints; }
    public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }
    
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    
    public Long getEpicId() { return epicId; }
    public void setEpicId(Long epicId) { this.epicId = epicId; }
    
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    
    public UserDto getAssignee() { return assignee; }
    public void setAssignee(UserDto assignee) { this.assignee = assignee; }
    
    public UserDto getReporter() { return reporter; }
    public void setReporter(UserDto reporter) { this.reporter = reporter; }
    
    public EpicDto getEpic() { return epic; }
    public void setEpic(EpicDto epic) { this.epic = epic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getSprintId() {

        return sprintId;

    }
 
    public void setSprintId(Long sprintId) {

        this.sprintId = sprintId;

    }
 
    public Long getProjectId() {

        return projectId;

    }
 
    public void setProjectId(Long projectId) {

        this.projectId = projectId;

    }
 
}