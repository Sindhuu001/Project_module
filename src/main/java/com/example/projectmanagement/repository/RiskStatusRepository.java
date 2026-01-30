package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.RiskStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RiskStatusRepository extends JpaRepository<RiskStatus, Long> {

    List<RiskStatus> findByProjectIdOrderBySortOrderAsc(Long projectId);

    boolean existsByProjectIdAndName(Long projectId, String name);
    List<RiskStatus> findByProjectIdOrderBySortOrder(Long projectId);

    @Query("SELECT MAX(rs.sortOrder) FROM RiskStatus rs WHERE rs.projectId = :projectId")
    Integer findMaxSortOrder(@Param("projectId") Long projectId);

}
