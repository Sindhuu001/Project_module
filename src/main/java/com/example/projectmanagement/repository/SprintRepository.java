package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);

    List<Sprint> findByStatus(Sprint.SprintStatus status);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = :status")
    List<Sprint> findByProjectIdAndStatus(@Param("projectId") Long projectId,
                                          @Param("status") Sprint.SprintStatus status);

    Optional<Sprint> findFirstByProjectIdAndStatus(Long projectId, Sprint.SprintStatus status);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId")
    Page<Sprint> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT s FROM Sprint s WHERE s.status = 'ACTIVE' AND s.project.id = :projectId")
    List<Sprint> findActiveSprintsByProject(@Param("projectId") Long projectId);

//    @Query("""
//       SELECT s
//       FROM Sprint s
//       WHERE s.project.id = :projectId
//         AND s.status = com.example.projectmanagement.entity.Sprint.SprintStatus.ACTIVE
//       """)
//    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") Long projectId);


    @Query("SELECT s FROM Sprint s WHERE s.endDate < :date AND s.status <> 'COMPLETED'")
    List<Sprint> findOverdueSprints(@Param("date") LocalDateTime date);

    // Check overlapping sprints
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId " +
            "AND (:startDate <= s.endDate AND :endDate >= s.startDate)")
    List<Sprint> findOverlappingSprints(@Param("projectId") Long projectId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Check if a project already has active sprint
    @Query("SELECT COUNT(s) > 0 FROM Sprint s WHERE s.project.id = :projectId AND s.status = 'ACTIVE'")
    boolean existsActiveSprintInProject(@Param("projectId") Long projectId);

    long countByEndDateBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByNameAndProjectId(String name, Long projectId);

    // Find expired active sprints
    List<Sprint> findByStatusAndEndDateBefore(Sprint.SprintStatus status,
                                              LocalDateTime before);

    // Find next sprint based on start date
    Optional<Sprint> findFirstByProjectIdAndStartDateAfterOrderByStartDateAsc(
            Long projectId, LocalDateTime after);

    Optional<Sprint> findFirstByProject_IdAndStartDateAfterOrderByStartDateAsc(Long projectId, LocalDateTime after);

    @Query("SELECT COUNT(s) FROM Sprint s WHERE s.endDate BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    long countSprintsEndingSoon(@Param("futureDate") LocalDateTime futureDate);

}
