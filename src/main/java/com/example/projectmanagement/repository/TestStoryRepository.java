package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.testing.TestStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestStoryRepository extends JpaRepository<TestStory, Long> {

    List<TestStory> findByProjectId(Long projectId);

    List<TestStory> findByLinkedUserStoryId(Long storyId);

//    int countDistinctStoriesByTestPlanId(Long planId);
}