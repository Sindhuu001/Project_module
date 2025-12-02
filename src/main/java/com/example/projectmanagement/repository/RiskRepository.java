package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {
    List<Risk> findByProjectId(Long projectId);
    boolean existsByProjectIdAndTitleAndCategoryId(Long projectId, String title, Long categoryId);
    boolean existsByProjectIdAndTitleAndCategoryIdAndIdNot(
            Long projectId, String title, Long categoryId, Long id);

    Page<Risk> findByRiskLinks_LinkedTypeAndRiskLinks_LinkedId(LinkedType linkedType, Long linkedId, Pageable pageable);

    @Query("""
        SELECT r FROM Risk r
        JOIN RiskLink rl ON rl.risk.id = r.id
        WHERE rl.linkedType = :linkedType
          AND rl.linkedId = :linkedId
          AND r.projectId = :projectId
        """)
    Page<Risk> findByLinkedEntity(
            @Param("projectId") Long projectId,
            @Param("linkedType") RiskLink.LinkedType linkedType,
            @Param("linkedId") Long linkedId,
            Pageable pageable
    );


}
