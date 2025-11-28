package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRunCaseRepository extends JpaRepository<TestRunCase, Long> {

    List<TestRunCase> findByRunId(Long runId);

    List<TestRunCase> findByRunIdAndAssigneeId(Long runId, Long assignedTo);

    int countByRunId(Long runId);

    int countByRunIdAndStatus(Long runId, TestRunCaseStatus status);

    List<TestRunCase> findByRunIdAndTestCaseIdIn(Long runId, List<Long> testCaseIds);

    Optional<TestRunCase> findByRunIdAndTestCaseId(Long runId, Long testCaseId);

    List<TestRunCase> findByRunIdAndStatus(Long runId, TestRunCaseStatus status);

    List<TestRunCase> findByIdIn(List<Long> ids);

}
