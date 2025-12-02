package com.example.projectmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "risks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_risk_project_title_category",
                        columnNames = {"project_id", "title", "category_id"}
                )
        }
)
@Data
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ========= PROJECT ========= */

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * Read-only relation for navigation / joins.
     * Actual FK value is controlled by projectId field.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    @JsonIgnore
    private Project project;

    /* ========= OTHER IDS ========= */

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    /* ========= BUSINESS DATA ========= */

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Byte probability;

    @Column(nullable = false)
    private Byte impact;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(length = 2000)
    private String triggers;

    /* ========= AUDIT ========= */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /* ========= LINKS ========= */

    @OneToMany(mappedBy = "risk", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RiskLink> riskLinks;
}
