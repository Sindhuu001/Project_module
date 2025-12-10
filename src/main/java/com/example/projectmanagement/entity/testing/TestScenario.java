package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.enums.TestPriority;
import com.example.projectmanagement.enums.TestScenarioStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_scenarios")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which test plan this scenario belongs to
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private TestPlan testPlan;

    // Optional grouping under a TestStory
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_story_id")
    private TestStory testStory;

    // Optional direct link to a User Story (must be consistent with testStory if both set)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story linkedUserStory;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestScenarioStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TestCase getProjectId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProjectId'");
    }
}
