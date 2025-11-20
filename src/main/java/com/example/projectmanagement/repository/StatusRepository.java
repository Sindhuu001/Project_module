package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByProjectIdOrderBySortOrder(Long projectId);

    Optional<Status> findTopByProjectIdOrderBySortOrderDesc(Long projectId);

    void deleteByProjectId(Long projectId);


}
