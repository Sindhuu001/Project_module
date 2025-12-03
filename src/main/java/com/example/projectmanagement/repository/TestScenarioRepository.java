package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestScenarioRepository extends JpaRepository<TestScenario, Long> {

    List<TestScenario> findByTestPlanId(Long planId);

    List<TestScenario> findByTestStoryId(Long testStoryId);

    List<TestScenario> findByLinkedUserStoryId(Long storyId);

    int countByTestPlanId(Long planId);

    @Query("SELECT COUNT(DISTINCT ts.linkedUserStory.id) FROM TestScenario ts WHERE ts.testPlan.id = :planId")
    int countDistinctStoriesByTestPlanId(@Param("planId") Long planId);

    @Query("SELECT COUNT(ts) FROM TestScenario ts WHERE ts.testStory.id = :storyId")
    int countByTestStoryId(@Param("storyId") Long storyId);
}
