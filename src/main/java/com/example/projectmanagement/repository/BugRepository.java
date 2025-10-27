package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {
    List<Bug> findByProjectId(Long projectId);
    List<Bug> findByAssignedTo(Long assignedTo);
    List<Bug> findBySprintId(Long sprintId);
    List<Bug> findByStatus(Bug.Status status);
    List<Bug> findBySeverity(Bug.Severity severity);
    List<Bug> findByEpicId(Long epicId);
    List<Bug> findByTaskId(Long taskId);

}
