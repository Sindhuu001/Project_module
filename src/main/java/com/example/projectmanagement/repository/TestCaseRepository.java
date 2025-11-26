package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findByScenarioId(Long scenarioId);

    int countByScenarioTestPlanId(Long planId);

    int countByScenarioId(Long ScenarioId);

    @Query("select tc.id from TestCase tc where tc.scenario.id = :scenarioId")
    List<Long> findCaseIdsByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("select tc.id from TestCase tc where tc.scenario.testStory.id = :testStoryId")
    List<Long> findCaseIdsByTestStoryId(@Param("testStoryId") Long testStoryId);

}
