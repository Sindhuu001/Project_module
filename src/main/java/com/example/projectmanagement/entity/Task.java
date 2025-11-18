package com.example.projectmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(
    name = "tasks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"title", "project_id", "story_id"})
)
@Data

public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    @Pattern(
        regexp = "^(?!.* {3,})[A-Za-z0-9 ]+$",
        message = "Name must contain only letters, digits, spaces, and not more than 2 consecutive spaces"
    )
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = true) // story optional
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = true) // sprint optional
    private Sprint sprint;

    private Long assigneeId;
    private Long reporterId;
    private boolean isBillable;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Transient getter for sprintId (derived from story)
    @Transient
    @JsonProperty("effectiveSprintId")
    public Long getEffectiveSprintId() {
        return sprint != null ? sprint.getId() : (story != null && story.getSprint() != null ? story.getSprint().getId() : null);
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Constructors
    public Task() {}

    public Task(String title, String description, Project project, Long reporterId) {
        this.title = title;
        this.description = description;
        this.project = project;
        this.reporterId = reporterId;
    }
}
