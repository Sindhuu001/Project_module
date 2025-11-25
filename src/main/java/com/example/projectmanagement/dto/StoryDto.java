package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Story;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StoryDto {
    
    private Long id;
    
    @NotBlank(message = "Story title is required")
    @Size(min = 2, max = 200, message = "Story title must be between 2 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private StatusDto status; // Replaced enum with DTO
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
   
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
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
}
