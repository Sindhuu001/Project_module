package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.graphs.SprintScopeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SprintScopeChangeRepository extends JpaRepository<SprintScopeChange, Long> {

    List<SprintScopeChange> findBySprintIdOrderByChangedAtAsc(Long sprintId);

    // Sum of positive deltas on a specific date (added scope)
    @Query("""
        SELECT COALESCE(SUM(c.pointsDelta), 0)
        FROM SprintScopeChange c
        WHERE c.sprint.id = :sprintId
          AND CAST(c.changedAt AS date) = :date
          AND c.pointsDelta > 0
    """)
    int sumAddedPointsOnDate(@Param("sprintId") Long sprintId, @Param("date") LocalDate date);

    // Sum of negative deltas on a specific date (removed scope) — returns negative number
    @Query("""
        SELECT COALESCE(SUM(c.pointsDelta), 0)
        FROM SprintScopeChange c
        WHERE c.sprint.id = :sprintId
          AND CAST(c.changedAt AS date) = :date
          AND c.pointsDelta < 0
    """)
    int sumRemovedPointsOnDate(@Param("sprintId") Long sprintId, @Param("date") LocalDate date);
}