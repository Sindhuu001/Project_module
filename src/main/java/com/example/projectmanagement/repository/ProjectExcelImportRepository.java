package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.ProjectExcelImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectExcelImportRepository extends JpaRepository<ProjectExcelImport, Long> {
    List<ProjectExcelImport> findByProjectIdOrderByUploadedAtDesc(Long projectId);
}
