package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.Task.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByProjectId(Long projectId);
    
    List<Task> findBySprintId(Long sprintId);
    
    List<Task> findByStoryId(Long storyId);
    
    List<Task> findByAssigneeId(Long assigneeId);
    
    List<Task> findByReporterId(Long reporterId);
    
    List<Task> findByStatus(Task.TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.sprint.id = :sprintId AND t.status = :status")
    List<Task> findBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId")
    Page<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:title%")
    Page<Task> findByTitleContaining(@Param("title") String title, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.priority = :priority")
    Page<Task> findByPriority(@Param("priority") Task.Priority priority, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.story.id = :storyId")
    long countByStoryId(@Param("storyId") Long storyId);

    
    @Query("SELECT t FROM Task t WHERE t.sprint IS NULL AND t.status IN ('BACKLOG', 'TODO')")
    List<Task> findBacklogTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    long countTasksDueSoon(@Param("futureDate") LocalDateTime futureDate);
    long countByStatus(Task.TaskStatus status);

    long countByDueDateBetween(LocalDateTime start, LocalDateTime end);

    long countByAssigneeId(Long userId);

    Long countByAssigneeIdAndStatus(Long userId, TaskStatus status);

}