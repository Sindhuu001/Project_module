package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.enums.TestCaseType;
import com.example.projectmanagement.enums.TestPriority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_cases")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id")
    private TestScenario scenario;

    @Column(nullable = false)
    private String title;

    @Column(name = "pre_conditions", columnDefinition = "text")
    private String preConditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestCaseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestPriority priority;

    @Column(nullable = false)
    private String status; // if you want, later turn into enum

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
