package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.IssueTypeRiskCountDTO;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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


}
