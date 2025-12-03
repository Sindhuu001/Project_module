package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mitigation_plans")
@Data
public class MitigationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @Column(columnDefinition = "TEXT")
    private String mitigation;

    @Column(columnDefinition = "TEXT")
    private String contingency;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt = LocalDateTime.now();

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "used")
    private Boolean used = false;

    @Column(name = "effective")
    private Boolean effective;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
