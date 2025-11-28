package com.example.projectmanagement.entity.testing;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_steps")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private TestCase testCase;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(columnDefinition = "text", nullable = false)
    private String action;

    @Column(name = "expected_result", columnDefinition = "text", nullable = false)
    private String expectedResult;
}
