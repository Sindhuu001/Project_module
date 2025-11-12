package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.entity.Statuses;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusesRepository extends JpaRepository<Statuses, Long> {

    List<Statuses> findByProjectIdOrderBySortOrder(Long projectId);

    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);

    @Query("SELECT s FROM Statuses s WHERE s.project.id = :projectId AND s.isPredefined = TRUE")
    List<Statuses> findPredefinedByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT new com.example.projectmanagement.dto.StatusDto(" +
            "s.statusId, s.name, s.sortOrder, s.isActive, s.isBug, s.isPredefined, s.project.id, s.project.name) " +
            "FROM Statuses s WHERE s.project.id = :projectId ORDER BY s.sortOrder")
    List<StatusDto> findAllDtoByProjectId(@Param("projectId") Long projectId);

}
