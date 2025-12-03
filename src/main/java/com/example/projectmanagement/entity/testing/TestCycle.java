package com.example.projectmanagement.entity.testing;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestCycleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_cycles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false)
    private TestCycleType cycleType;

    @Enumerated(EnumType.STRING)
    private TestCycleStatus status;


    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;
}
