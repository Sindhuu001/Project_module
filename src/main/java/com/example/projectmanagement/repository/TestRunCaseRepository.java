package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ADD to TestRunCaseRepository:
    List<TestRunCase> findByAssigneeIdAndStatusNot(Long assigneeId, TestRunCaseStatus status);
//    List<TestRunCase> findByRunId(Long runId);  // likely already exists, verify

    @Modifying
    @Query("DELETE FROM TestRunCase t WHERE t.run.id = :runId")
    void deleteByRunId(@Param("runId") Long runId);

    @Modifying
    @Query("DELETE FROM TestRunCase t WHERE t.id = :runCaseId")
    void deleteByRunCaseId(@Param("runCaseId") Long runCaseId);

}
