package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.graphs.SprintBurndownSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintBurndownSnapshotRepository extends JpaRepository<SprintBurndownSnapshot, Long> {

    List<SprintBurndownSnapshot> findBySprintIdOrderBySnapshotDateAsc(Long sprintId);

    Optional<SprintBurndownSnapshot> findBySprintIdAndSnapshotDate(Long sprintId, LocalDate date);

    Optional<SprintBurndownSnapshot> findFirstBySprintIdOrderBySnapshotDateAsc(Long sprintId);

    boolean existsBySprintIdAndSnapshotDate(Long sprintId, LocalDate date);
}