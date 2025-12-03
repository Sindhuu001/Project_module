package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskCategoryCreateRequest;
import com.example.projectmanagement.dto.RiskCategoryResponse;
import com.example.projectmanagement.service.RiskCategoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk/category")
@RequiredArgsConstructor
@CrossOrigin
public class RiskManagementController {

    private final RiskCategoryService riskCategoryService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskCategoryResponse> createRiskCategory(
            @Valid @RequestBody RiskCategoryCreateRequest request
    ) {
        return new ResponseEntity<>(
                riskCategoryService.createCategory(request),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<RiskCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(riskCategoryService.getAllCategories());
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(riskCategoryService.getCategoryById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody RiskCategoryCreateRequest request
    ) {
        return ResponseEntity.ok(riskCategoryService.updateCategory(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        riskCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
