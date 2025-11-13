package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.Task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("""
    SELECT new com.example.projectmanagement.dto.TaskDto$Summary(t.id,t.title,t.status,t.story.id,t.story.sprint.id,t.priority,t.reporterId,t.assigneeId,t.createdAt,t.isBillable,t.dueDate)
    FROM Task t
    WHERE t.project.id = :projectId""")
    List<TaskDto.Summary> findTaskSummariesByProjectId(@Param("projectId") Long projectId);

    List<Task> findByStoryId(Long storyId);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByReporterId(Long reporterId);

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    // ✅ fetch tasks indirectly linked to a sprint via their story
    @Query("SELECT t FROM Task t WHERE t.story.sprint.id = :sprintId")
    List<Task> findBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT t FROM Task t WHERE t.story.sprint.id = :sprintId AND t.status = :status")
    List<Task> findBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId")
    Page<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.title LIKE %:title%")
    Page<Task> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.priority = :priority")
    Page<Task> findByPriority(@Param("priority") Task.Priority priority, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.story.id = :storyId")
    long countByStoryId(@Param("storyId") Long storyId);

    @Query("SELECT t FROM Task t WHERE t.story.sprint IS NULL AND t.status IN ('BACKLOG', 'TODO')")
    List<Task> findBacklogTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    long countTasksDueSoon(@Param("futureDate") LocalDateTime futureDate);

    @Query("SELECT new com.example.projectmanagement.dto.TaskDto$Summary(t.id, t.title, t.status, t.story.id, t.story.sprint.id) " +
       "FROM Task t WHERE t.story.sprint.id = :sprintId")
    List<TaskDto.Summary> findTaskSummariesBySprintId(@Param("sprintId") Long sprintId);

    @Modifying
    @Query("UPDATE Task t SET t.status = :status WHERE t.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Task.TaskStatus status);


    long countByStatus(TaskStatus status);

    long countByDueDateBetween(LocalDateTime start, LocalDateTime end);

    long countByAssigneeId(Long userId);

    Long countByAssigneeIdAndStatus(Long userId, TaskStatus status);
}
