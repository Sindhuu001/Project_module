package com.example.projectmanagement.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sprints", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "project_id"})
})
@Data
public class Sprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Sprint name is required")
    @Size(min = 2, max = 100, message = "Sprint name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "Goal cannot exceed 500 characters")
    private String goal;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status = SprintStatus.PLANNING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SprintStatus {
        PLANNING, ACTIVE, COMPLETED
    }

    private Long startedBy;

    private LocalDateTime startedAt;

    
    // Constructors
    public Sprint() {
    }
    
    
    public Sprint(String name, String goal, LocalDateTime startDate, LocalDateTime endDate, Project project) {
        this.name = name;
        this.goal = goal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.project = project;
    }    
}