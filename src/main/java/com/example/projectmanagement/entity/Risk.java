package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
