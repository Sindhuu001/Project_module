package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RiskEpicRepository extends JpaRepository<Epic, Long> {

    @Query(
            value = """
        SELECT new com.example.projectmanagement.dto.RiskIssueSummaryDTO(
            'Epic',
            e.id,
            CONCAT(e.name),
            s.name,
            NULL,
            COUNT(rl.id)
        )
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Epic e ON e.id = rl.linkedId
        JOIN e.status s
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (
              :search IS NULL
              OR LOWER(e.name) LIKE CONCAT('%', :search, '%')
              OR LOWER(CONCAT('Epic-', e.id, ' ', e.name)) LIKE CONCAT('%', :search, '%')
          )
        GROUP BY e.id, e.name, s.name
        """,
            countQuery = """
        SELECT COUNT(DISTINCT e.id)
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Epic e ON e.id = rl.linkedId
        JOIN e.status s
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (
              :search IS NULL
              OR LOWER(e.name) LIKE CONCAT('%', :search, '%')
              OR LOWER(CONCAT('Epic-', e.id, ' ', e.name)) LIKE CONCAT('%', :search, '%')
          )
        """
    )
    Page<RiskIssueSummaryDTO> findEpicsWithRiskSummary(
            @Param("projectId") Long projectId,
            @Param("linkedType") LinkedType linkedType,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}