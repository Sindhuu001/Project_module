package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.MitigationPlan;
import com.example.projectmanagement.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MitigationPlanRepository extends JpaRepository<MitigationPlan, Long> {


    boolean existsByRisk(Risk risk);
    List<MitigationPlan> findByRisk(Risk risk);

    boolean existsByRiskAndIdNot(Risk risk, Long id);
    boolean existsByRiskAndMitigation(Risk risk, String mitigation);


}
