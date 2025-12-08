package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    List<Story> findByEpicId(Long epicId);
    List<Story> findByProjectId(Long projectId);
    List<Story> findByStatusId(Long statusId); // Replaced findByStatus
    
    List<Story> findByAssigneeId(Long assigneeId);

    @Query("SELECT s from Story s WHERE s.id = :storyId")
    Story findStorybyStoryId(@Param("storyId")Long storyId);

    
    List<Story> findByReporterId(Long reporterId);
    List<Story> findBySprintId(Long sprintId);

    List<Story> findByEpicIsNullAndProjectIdAndSprintIdIsNull(Long projectId);

    @Query("SELECT s FROM Story s WHERE s.epic.id = :epicId")
    Page<Story> findByEpicId(@Param("epicId") Long epicId, Pageable pageable);
    
    @Query("SELECT s FROM Story s WHERE s.title LIKE %:title%")
    Page<Story> findByTitleContaining(@Param("title") String title, Pageable pageable);
    
    @Query("SELECT s FROM Story s WHERE s.priority = :priority")
    Page<Story> findByPriority(@Param("priority") Story.Priority priority, Pageable pageable);
    
    long countByStatusId(Long statusId); // Replaced countByStatus

    @Query("SELECT s FROM Story s " +
            "WHERE (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:priority IS NULL OR s.priority = :priority) " +
            "AND (:epicId IS NULL OR s.epic.id = :epicId) " +
            "AND (:sprintId IS NULL OR s.sprint.id = :sprintId) " +
            "AND (:projectId IS NULL OR s.project.id = :projectId)")
    Page<Story> searchByFilters(
        @Param("title") String title,
        @Param("priority") Story.Priority priority,
        @Param("epicId") Long epicId,
        @Param("sprintId") Long sprintId,
        @Param("projectId") Long projectId,
        Pageable pageable
    );
    boolean existsByTitleAndProjectIdAndEpicId(String title, Long projectId, Long epicId);
    Long countByAssigneeIdAndStatusId(Long userId, Long statusId); // Replaced countByAssigneeIdAndStatus

    @Query("""
        SELECT s FROM Story s
        WHERE s.sprint.id = :sprintId
          AND s.status.sortOrder <> :finalSortOrder
    """)
    List<Story> findIncompleteStoriesBySprintId(
            @Param("sprintId") Long sprintId,
            @Param("finalSortOrder") Integer finalSortOrder);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Story s
        WHERE s.sprint.id = :sprintId
          AND s.status.sortOrder <> :finalSortOrder
       """)
    boolean existsBySprintIdAndStatus_SortOrderNot(@Param("sprintId") Long sprintId,
                                                   @Param("finalSortOrder") Integer finalSortOrder);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Story s " +
            "WHERE s.sprint.id = :sprintId AND s.tasks IS EMPTY")
    boolean existsBySprintIdWithNoTasks(@Param("sprintId") Long sprintId);

    long countBySprintId(Long id);
}
