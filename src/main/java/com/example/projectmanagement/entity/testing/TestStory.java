package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Story;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_stories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    // optional – for exploratory / non-functional tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story linkedUserStory;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
