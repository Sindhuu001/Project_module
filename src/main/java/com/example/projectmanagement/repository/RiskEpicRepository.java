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
            CONCAT('Epic-', e.id, ' ', e.name),
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
        """
    )
    Page<RiskIssueSummaryDTO> findEpicsWithRiskSummary(
            @Param("projectId") Long projectId,
            @Param("linkedType") LinkedType linkedType,
            @Param("status") String status,
            Pageable pageable
    );

}
