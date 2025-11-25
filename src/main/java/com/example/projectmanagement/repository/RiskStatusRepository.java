package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskStatusRepository extends JpaRepository<RiskStatus, Long> {

    List<RiskStatus> findByProjectIdOrderBySortOrderAsc(Long projectId);

    boolean existsByProjectIdAndName(Long projectId, String name);
    List<RiskStatus> findByProjectIdOrderBySortOrder(Long projectId);



}
