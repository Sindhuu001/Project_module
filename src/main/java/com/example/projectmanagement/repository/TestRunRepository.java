package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.enums.TestRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    List<TestRun> findByCycleId(Long cycleId);

    int countByCycleId(Long cycleId);

    int countByCycleIdAndStatus(Long cycleId, String status);// or enum if you use one

//    int countByCycle_IdAndStatus(Long cycleId, TestRunStatus status);

    List<TestRun> findByCycleIdOrderByCreatedAtAsc(Long cycleId);

}
