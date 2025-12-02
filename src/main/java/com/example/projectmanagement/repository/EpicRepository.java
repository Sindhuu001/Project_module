package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {

    List<Epic> findByProjectId(Long projectId);

    List<Epic> findByStatus(Status status);
    
    @Query("SELECT e FROM Epic e WHERE e.id = :epicId")
    Epic findEpicBasicById(@Param("epicId") Long epicId);

    @Query("SELECT e FROM Epic e WHERE e.project.id = :projectId AND e.status.id = :statusId")
    List<Epic> findByProjectIdAndStatusId(
            @Param("projectId") Long projectId,
            @Param("statusId") Long statusId
    );

    List<Epic> findByStatusId(Long StatusId);

    @Query("SELECT e FROM Epic e WHERE e.project.id = :projectId")
    Page<Epic> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT e FROM Epic e WHERE e.name LIKE %:name%")
    Page<Epic> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT e FROM Epic e WHERE e.priority = :priority")
    Page<Epic> findByPriority(@Param("priority") Epic.Priority priority, Pageable pageable);
    boolean existsByNameAndProjectId(String name, Long projectId);
}
