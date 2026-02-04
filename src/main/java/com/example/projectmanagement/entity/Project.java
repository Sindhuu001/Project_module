package com.example.projectmanagement.entity;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
 
@Entity
@Table(name = "projects")
@Data
public class Project {
 
    /* =====================
       PRIMARY KEY
       ===================== */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    /* =====================
       CORE PROJECT INFO (FROM EXISTING)
       ===================== */
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100)
    @Pattern(
        regexp = "^(?!.* {3,})[A-Za-z0-9 ]+$",
        message = "Name must contain only letters, digits, spaces, and not more than 2 consecutive spaces"
    )
    @Column(nullable = false)
    private String name;
 
    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10)
    @Column(unique = true, nullable = false)
    private String projectKey;
 
    @Size(max = 500)
    private String description;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage")
    private ProjectStage currentStage = ProjectStage.INITIATION;
 
    /* =====================
       OWNERSHIP & IDENTITY (OURS TAKES PRIORITY)
       ===================== */
    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

 
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
 
    @Column(name = "rm_id")
    private Long rmId;
 
    @Column(name = "delivery_owner_id")
    private Long deliveryOwnerId;
 
    /* =====================
       DELIVERY / RISK / PRIORITY (OURS)
       ===================== */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_model", length = 20)
    private DeliveryModel deliveryModel;
 
    @Column(name = "primary_location", length = 100)
    private String primaryLocation;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;
 
    @Column(name = "risk_level_updated_at")
    private LocalDateTime riskLevelUpdatedAt;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", length = 20)
    private PriorityLevel priorityLevel;
 
    /* =====================
       BUDGET (OURS)
       ===================== */
    @Column(name = "project_budget", precision = 15, scale = 2)
    private BigDecimal projectBudget;
 
    @Column(name = "project_budget_currency", length = 3)
    private String projectBudgetCurrency;
 
    /* =====================
       MEMBERS & RELATIONS (FROM EXISTING)
       ===================== */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Set<Long> memberIds = new HashSet<>();
 
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Epic> epics = new ArrayList<>();
 
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Sprint> sprints = new ArrayList<>();
 
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Task> tasks = new ArrayList<>();
 
    /* =====================
       DATES & AUDIT (FROM EXISTING)
       ===================== */
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
 
    /* =====================
       ENUMS
       ===================== */
    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        PLANNING,
        COMPLETED
    }
 
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
 
    public enum DeliveryModel {
        ONSITE,
        OFFSHORE,
        HYBRID
    }
 
    public enum PriorityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
 
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
 
    /* =====================
       CONSTRUCTORS
       ===================== */
    public Project() {
    }
 
    public Project(
            String name,
            String projectKey,
            String description,
            String clientId,
            Long ownerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        this.name = name;
        this.projectKey = projectKey;
        this.description = description;
        this.clientId = clientId;
        this.ownerId = ownerId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
