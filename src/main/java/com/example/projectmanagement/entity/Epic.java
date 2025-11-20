package com.example.projectmanagement.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "epic", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "project_id"})
})
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(
    regexp = "^(?!.* {3,})[A-Za-z0-9 ]+$",
    message = "Name must contain only letters, digits, spaces, and not more than 2 consecutive spaces"
    )
    private String name;

    @Column(length = 1000)
    private String description;

//    @Enumerated(EnumType.STRING)
//    private EpicStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Builder.Default
    private Integer progressPercentage=0;


    private LocalDate dueDate;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Story> stories = new ArrayList<>();

    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Enums
    
//    public enum EpicStatus {
//        OPEN,
//        IN_PROGRESS,
//        COMPLETED,
//        ON_HOLD
//    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
