package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskCategoryRepository extends JpaRepository<RiskCategory, Long> {

    Optional<RiskCategory> findByName(String name);
}
