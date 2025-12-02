package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.entity.Story;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RiskStoryRepository extends JpaRepository<Story, Long> {

    @Query(
            value = """
        SELECT new com.example.projectmanagement.dto.RiskIssueSummaryDTO(
            'Story',
            st.id,
            CONCAT('Story-', st.id, ' ', st.title),
            s.name,
            sp.id,
            COUNT(rl.id)
        )
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Story st ON st.id = rl.linkedId
        JOIN st.status s
        LEFT JOIN st.sprint sp
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (:sprintId IS NULL OR sp.id = :sprintId)
        GROUP BY st.id, st.title, s.name, sp.id
        """,
            countQuery = """
        SELECT COUNT(DISTINCT st.id)
        FROM Risk r
        JOIN r.riskLinks rl
        JOIN Story st ON st.id = rl.linkedId
        JOIN st.status s
        LEFT JOIN st.sprint sp
        WHERE r.project.id = :projectId
          AND rl.linkedType = :linkedType
          AND (:status IS NULL OR s.name = :status)
          AND (:sprintId IS NULL OR sp.id = :sprintId)
        """
    )
    Page<RiskIssueSummaryDTO> findStoriesWithRiskSummary(
            @Param("projectId") Long projectId,
            @Param("linkedType") LinkedType linkedType,
            @Param("status") String status,
            @Param("sprintId") Long sprintId,
            Pageable pageable
    );
}
