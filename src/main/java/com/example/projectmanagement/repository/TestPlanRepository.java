package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestPlanRepository extends JpaRepository<TestPlan, Long> {

    List<TestPlan> findByProjectId(Long projectId);
}