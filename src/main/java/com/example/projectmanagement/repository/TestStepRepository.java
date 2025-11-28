package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestStepRepository extends JpaRepository<TestStep, Long> {

    List<TestStep> findByTestCaseIdOrderByStepNumberAsc(Long caseId);

    int countByTestCaseId(Long testCaseId);
}
