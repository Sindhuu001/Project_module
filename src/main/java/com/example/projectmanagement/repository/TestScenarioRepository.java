package com.example.projectmanagement.repository;
import com.example.projectmanagement.entity.testing.TestScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestScenarioRepository extends JpaRepository<TestScenario, Long> {

    List<TestScenario> findByTestPlanId(Long planId);

    List<TestScenario> findByTestStoryId(Long testStoryId);

    List<TestScenario> findByLinkedUserStoryId(Long storyId);

    int countByTestPlanId(Long planId);
    int countDistinctStoriesByTestPlanId(Long planId);
    int countByTestStoryId(Long StoryId);
}
