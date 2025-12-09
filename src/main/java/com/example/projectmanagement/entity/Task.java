package com.example.projectmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Size(min = 2, max = 200)
    @Pattern(
            regexp = "^(?!.* {3,})[A-Za-z0-9 ]+$",
            message = "Name must contain only letters, digits, spaces, and not more than 2 consecutive spaces"
    )
    @Column(nullable = false)
    private String title;

    @Size(max = 1000)
    private String description;

    /* ------------------------
       Relations (IGNORED)
    ------------------------ */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    @JsonIgnore   // ✅ status details fetched via separate API if needed
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference   // ✅ prevents Project → Task → Project loop
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    @JsonIgnore
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    @JsonIgnore
    private Sprint sprint;

    /* ------------------------
       Simple Fields
    ------------------------ */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    private Long assigneeId;
    private Long reporterId;
    private boolean billable;

    /* ------------------------
       Audit Fields
    ------------------------ */

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ------------------------
       Child Collections
    ------------------------ */

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore   // ✅ comments fetched via separate endpoint
    private List<Comment> comments = new ArrayList<>();

    /* ------------------------
       Derived Transient Fields
    ------------------------ */

    @Transient
    @JsonProperty("effectiveSprintId")
    public Long getEffectiveSprintId() {
        if (sprint != null) return sprint.getId();
        if (story != null && story.getSprint() != null) return story.getSprint().getId();
        return null;
    }

    /* ------------------------
       Enums
    ------------------------ */

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /* ------------------------
       Constructors
    ------------------------ */

    public Task() {}

    public Task(String title, String description, Project project, Long reporterId) {
        this.title = title;
        this.description = description;
        this.project = project;
        this.reporterId = reporterId;
    }
}
