package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.ExcelImportResultDto;
import com.example.projectmanagement.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/excel-import")
@RequiredArgsConstructor
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelImportResultDto> uploadExcel(
            @RequestParam("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {

        ExcelImportResultDto result = excelImportService.importFromExcel(projectId, file, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ExcelImportResultDto>> getImportsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(excelImportService.getImportsByProject(projectId));
    }
}
