package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByProjectIdOrderBySortOrder(Long projectId);

    Optional<Status> findTopByProjectIdOrderBySortOrderDesc(Long projectId);

    void deleteByProjectId(Long projectId);

    @Query("SELECT MAX(s.sortOrder) FROM Status s WHERE s.project.id = :projectId")
    Integer findMaxSortOrderByProject(@Param("projectId") Long projectId);

    Status findFirstByProjectIdOrderBySortOrderDesc(Long projectId);
    @Query("SELECT s FROM Status s WHERE s.id=:statusId")
    Status findStatusById(@Param("statusId") Long statusId);

   @Query("SELECT s.sortOrder FROM Status s WHERE s.name = 'Done' or s.name = 'DONE'")
Integer findDoneSortOrder();


}
