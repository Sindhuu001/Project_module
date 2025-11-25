package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "risk_links", uniqueConstraints = {
        @UniqueConstraint(name = "uk_risk_link", columnNames = {"risk_id", "linked_type", "linked_id"})
})
@Data
public class RiskLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @Enumerated(EnumType.STRING)
    @Column(name = "linked_type", nullable = false)
    private LinkedType linkedType;

    @Column(name = "linked_id", nullable = false)
    private Long linkedId;

    public enum LinkedType {
        Epic, Sprint, Story, Task, Bug, Release
    }
}
