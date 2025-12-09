package com.example.projectmanagement.repository;

import com.example.projectmanagement.dto.ProjectSummary;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Project.ProjectStatus;

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
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByNameContainingAndStatus(String name, Project.ProjectStatus status, Pageable pageable);

    Optional<Project> findByProjectKey(String projectKey);

    List<Project> findByStatus(Project.ProjectStatus status);

    // ✅ ownerId is now just a column, not a relation
    List<Project> findByOwnerId(Long ownerId);

    // ✅ memberIds is stored as a List<Long>, so we must use a query
    @Query("SELECT p FROM Project p WHERE :userId IN elements(p.memberIds)")
    List<Project> findByMemberId(@Param("userId") Long userId);

    @Query("SELECT p FROM Project p WHERE p.name LIKE %:name%")
    Page<Project> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.status = :status")
    Page<Project> findByStatus(@Param("status") Project.ProjectStatus status, Pageable pageable);

    // ✅ fixed to use ownerId column directly
    @Query("SELECT COUNT(p) FROM Project p WHERE p.ownerId IS NULL")
    long countProjectsWithoutOwner();

    long countByOwnerIdIsNull();

    boolean existsByProjectKey(String projectKey);

    List<Project> findByMemberIdsAndStatus(Long userId, ProjectStatus active);

    Long countByStatus(Project.ProjectStatus status);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Project p " +
            "WHERE p.id = :projectId AND :userId IN elements(p.memberIds)")
    boolean isUserMemberOfProject(@Param("projectId") Long projectId,
                                  @Param("userId") Long userId);

    @Query("""
       SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END
       FROM Project p
       WHERE p.id = :projectId 
         AND (p.ownerId = :userId OR :userId IN elements(p.memberIds))
       """)
    boolean isUserPartOfProject(@Param("projectId") Long projectId,
                                @Param("userId") Long userId);

    @Query("SELECT p.id AS id, p.name AS name, p.projectKey AS projectKey, p.description AS description, p.status AS status " +
            "FROM Project p WHERE p.ownerId = :ownerId")
    List<ProjectSummary> findProjectSummariesByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT DISTINCT p.id AS id, p.name AS name, p.projectKey AS projectKey, " +
            "p.description AS description, p.status AS status " +
            "FROM Project p WHERE p.ownerId = :userId OR :userId MEMBER OF p.memberIds")
    List<ProjectSummary> findAccessibleProjectSummaries(@Param("userId") Long userId);
    @Query("SELECT p FROM Project p " +
        "WHERE p.ownerId = :ownerId " +
        "AND p.startDate <= :monthEnd " +
        "AND (p.endDate IS NULL OR p.endDate >= :monthStart)")
    List<Project> findActiveProjectsByPeriod(Long ownerId,
                                                LocalDateTime monthStart,
                                                LocalDateTime monthEnd);

    @Query("""
   SELECT DISTINCT p
   FROM Project p
   WHERE p.ownerId = :userId
      OR :userId MEMBER OF p.memberIds
""")
    List<Project> findByOwnerIdOrMemberId(@Param("userId") Long userId);




}
