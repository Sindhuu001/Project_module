package com.example.projectmanagement.entity;

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
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    @Pattern(
        regexp = "^(?!.* {3,})[A-Za-z0-9 ]+$",
        message = "Name must contain only letters, digits, spaces, and not more than 2 consecutive spaces"
    )
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    @Column(unique = true, nullable = false)
    private String projectKey;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    // 🧩 New field: currentStage (optional)
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = true)
    private ProjectStage currentStage=ProjectStage.INITIATION;  // e.g., PLANNING, DESIGN, DEVELOPMENT, etc.

    @Column(name = "ownerId", nullable = false)
    private Long ownerId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "user_id")
    private List<Long> memberIds = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Epic> epics = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sprint> sprints;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Project Status Enum
    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        PLANNING,
        COMPLETED
    }

    // 🧭 Project Stage Enum (optional)
    public enum ProjectStage {
        INITIATION,
        PLANNING,
        DESIGN,
        DEVELOPMENT,
        TESTING,
        DEPLOYMENT,
        MAINTENANCE,
        COMPLETED
    }

    public Project() {
    }

    public Project(String name, String projectKey, String description, Long ownerId,
                   LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.projectKey = projectKey;
        this.description = description;
        this.ownerId = ownerId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
