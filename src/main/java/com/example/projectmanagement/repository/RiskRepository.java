package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {
    List<Risk> findByProjectId(Long projectId);
    boolean existsByProjectIdAndTitleAndCategoryId(Long projectId, String title, Long categoryId);
    boolean existsByProjectIdAndTitleAndCategoryIdAndIdNot(
            Long projectId, String title, Long categoryId, Long id);
}
