package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_statuses")
@Data
public class RiskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotBlank(message = "Status name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Sort order is required")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
