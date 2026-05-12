package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.entity.Task;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RiskTaskRepository extends JpaRepository<Task, Long> {

    @Query(
            value = """
        SELECT new com.example.projectmanagement.dto.RiskIssueSummaryDTO(
            'Task',
            t.id,
            CONCAT(t.title),
            s.name,
            sp.id,
            COUNT(rl.id)
        )
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Task t ON t.id = rl.linkedId
        JOIN t.status s
        LEFT JOIN t.sprint sp
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (:sprintId IS NULL OR sp.id = :sprintId)
          AND (
              :search IS NULL
              OR LOWER(t.title) LIKE CONCAT('%', :search, '%')
              OR LOWER(CONCAT('Task-', t.id, ' ', t.title)) LIKE CONCAT('%', :search, '%')
          )
        GROUP BY t.id, t.title, s.name, sp.id
        """,
            countQuery = """
        SELECT COUNT(DISTINCT t.id)
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Task t ON t.id = rl.linkedId
        JOIN t.status s
        LEFT JOIN t.sprint sp
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (:sprintId IS NULL OR sp.id = :sprintId)
          AND (
              :search IS NULL
              OR LOWER(t.title) LIKE CONCAT('%', :search, '%')
              OR LOWER(CONCAT('Task-', t.id, ' ', t.title)) LIKE CONCAT('%', :search, '%')
          )
        """
    )
    Page<RiskIssueSummaryDTO> findTasksWithRiskSummary(
            @Param("projectId") Long projectId,
            @Param("linkedType") LinkedType linkedType,
            @Param("status") String status,
            @Param("sprintId") Long sprintId,
            @Param("search") String search,
            Pageable pageable
    );
}