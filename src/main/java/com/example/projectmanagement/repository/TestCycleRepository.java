package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCycleRepository extends JpaRepository<TestCycle, Long> {

    List<TestCycle> findByProjectId(Long projectId);
}
