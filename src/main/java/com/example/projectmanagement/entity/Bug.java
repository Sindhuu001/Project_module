package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bugs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private String title;
    @Column(length = 2000)
    private String description;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String type;

    private Long assignedTo; 
    @NotNull // userId
    private Long reporter; 
    @NotNull   // userId
    private Long projectId;

    private Long sprintId;

    private Long epicId;
    private Long taskId;


    @Column(length = 2000)
    private String stepsToReproduce;

    @Column(length = 1000)
    private String expectedResult;

    @Column(length = 1000)
    private String actualResult;

    private String attachments; // could store file path or URL
    @NotNull
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime resolvedDate;

    // Enums for Status, Priority, and Severity
    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Severity {
        MINOR, MAJOR, BLOCKER
    }
}
