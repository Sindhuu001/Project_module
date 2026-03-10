package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRunCaseStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestRunCaseStepRepository extends JpaRepository<TestRunCaseStep, Long> {

    List<TestRunCaseStep> findByRunCaseIdOrderByStepNumberAsc(Long runCaseId);

    Optional<TestRunCaseStep> findByRunCaseIdAndStepId(Long runCaseId, Long stepId);

    boolean existsByRunCaseId(Long runCaseId);

    int countByRunCaseId(Long runCaseId);

    @Modifying(clearAutomatically = true)
    @Query("""
                DELETE FROM TestRunCaseStep s
                WHERE s.runCase.run.cycle.id = :cycleId
            """)
    void deleteByRunCycleId(@Param("cycleId") Long cycleId);
}
