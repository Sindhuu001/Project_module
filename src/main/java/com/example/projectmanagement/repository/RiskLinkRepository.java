package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.IssueTypeRiskCountDTO;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RiskLinkRepository extends JpaRepository<RiskLink, Long> {
    List<RiskLink> findByRisk(Risk risk);

    boolean existsByRiskAndLinkedTypeAndLinkedId(Risk risk, RiskLink.LinkedType linkedType, Long linkedId);

    @Query("""
    SELECT new com.example.projectmanagement.dto.IssueTypeRiskCountDTO(
        rl.linkedType,
        COUNT(rl.id)
    )
    FROM RiskLink rl
    WHERE rl.risk.projectId = :projectId
    GROUP BY rl.linkedType
""")
    List<IssueTypeRiskCountDTO> countRisksByIssueType(
            @Param("projectId") Long projectId
    );

    @Query("""
    SELECT rl FROM RiskLink rl
    WHERE
        (rl.linkedType = 'Sprint' AND rl.linkedId = :sprintId)
     OR (rl.linkedType = 'Story' AND rl.linkedId IN :storyIds)
     OR (rl.linkedType = 'Task'  AND rl.linkedId IN :taskIds)
""")
    List<RiskLink> findRelevantSprintRiskLinks(
            @Param("sprintId") Long sprintId,
            @Param("storyIds") List<Long> storyIds,
            @Param("taskIds") Set<Long> taskIds
    );

}
