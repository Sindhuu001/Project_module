package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_plans")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String objective;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}


