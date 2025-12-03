package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Task;
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

    List<Task> findByStatusId(Long statusId);

    List<Task> findByProjectId(Long projectId); // Added method

    // ✅ fetch tasks indirectly linked to a sprint via their story
    @Query("SELECT t FROM Task t WHERE t.story.sprint.id = :sprintId")
    List<Task> findBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId")
    Page<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.title LIKE %:title%")
    Page<Task> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.priority = :priority")
    Page<Task> findByPriority(@Param("priority") Task.Priority priority, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.story.id = :storyId")
    long countByStoryId(@Param("storyId") Long storyId);


    @Query("SELECT t FROM Task t WHERE t.sprint IS NULL")
    List<Task> findBacklogTasks();


    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    long countTasksDueSoon(@Param("futureDate") LocalDateTime futureDate);

    @Query("SELECT new com.example.projectmanagement.dto.TaskDto$Summary(t.id, t.title, t.status, t.story.id, t.story.sprint.id) " +
       "FROM Task t WHERE t.story.sprint.id = :sprintId")
    List<TaskDto.Summary> findTaskSummariesBySprintId(@Param("sprintId") Long sprintId);

    long countByStatusId(Long statusId);

    long countByDueDateBetween(LocalDateTime start, LocalDateTime end);

    long countByAssigneeId(Long userId);

    Long countByAssigneeIdAndStatusId(Long userId, Long statusId);

    @Query("""
        SELECT t FROM Task t
        WHERE (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
    """)
    Page<Task> searchTasks(
            @Param("title") String title,
            @Param("priority") Task.Priority priority,
            @Param("assigneeId") Long assigneeId,
            Pageable pageable);

    // Checks whether there exists at least one task in sprint where task.status.sortOrder != maxSortOrder
    boolean existsBySprintIdAndStatus_SortOrderNot(Long sprintId, Integer sortOrder);

    // Get list of tasks in sprint which are NOT in final status (i.e., need transfer)
    @Query("SELECT t FROM Task t WHERE t.sprint.id = :sprintId AND t.status.sortOrder <> :finalSortOrder")
    List<Task> findIncompleteTasksBySprintId(Long sprintId, Integer finalSortOrder);

    boolean existsBySprintIdAndStatus_SortOrderLessThan(Long sprintId, Integer finalSortOrder);

    boolean existsBySprintIdAndStatusSortOrderNot(Long sprintId, Integer sortOrder);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t " +
            "JOIN t.status s " +
            "WHERE t.sprint.id = :sprintId " +
            "AND s.sortOrder <> :sortOrder")
    boolean existsTaskWithSprintIdAndStatusSortOrderNot(@Param("sprintId") Long sprintId,
                                                        @Param("sortOrder") Integer sortOrder);
}



