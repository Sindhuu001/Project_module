package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAttachmentRepository extends JpaRepository<RiskAttachment, Long> {
    List<RiskAttachment> findByRisk(Risk risk);
}
