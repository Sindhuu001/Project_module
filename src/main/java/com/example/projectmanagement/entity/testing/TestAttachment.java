package com.example.projectmanagement.entity.testing;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_attachments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_result_id")
    private TestResult testResult;

    @Column(name = "file_path", nullable = false)
    private String filePath;
}
