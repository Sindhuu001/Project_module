package com.example.projectmanagement.entity.testing;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_comments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_result_id")
    private TestResult testResult;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
