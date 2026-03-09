package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.enums.TestRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    List<TestRun> findByCycleId(Long cycleId);

    int countByCycleId(Long cycleId);

    int countByCycleIdAndStatus(Long cycleId, String status);// or enum if you use one

    // int countByCycle_IdAndStatus(Long cycleId, TestRunStatus status);

    List<TestRun> findByCycleIdOrderByCreatedAtAsc(Long cycleId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TestRun t WHERE t.cycle.id = :cycleId")
    void deleteByCycleId(@Param("cycleId") Long cycleId);
    
    List<TestRun> findByCreatedByAndStatusNot(Long assigneeId, TestRunStatus status);

}
