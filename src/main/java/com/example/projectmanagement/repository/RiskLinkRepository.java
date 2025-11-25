package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskLinkRepository extends JpaRepository<RiskLink, Long> {
    List<RiskLink> findByRisk(Risk risk);

    boolean existsByRiskAndLinkedTypeAndLinkedId(Risk risk, RiskLink.LinkedType linkedType, Long linkedId);
}
