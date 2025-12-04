package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRunCaseStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRunCaseStepRepository extends JpaRepository<TestRunCaseStep, Long> {

    List<TestRunCaseStep> findByRunCaseIdOrderByStepNumberAsc(Long runCaseId);

    Optional<TestRunCaseStep> findByRunCaseIdAndStepId(Long runCaseId, Long stepId);

    boolean existsByRunCaseId(Long runCaseId);

    int countByRunCaseId(Long runCaseId);
}
