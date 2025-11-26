package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskCategoryCreateRequest;
import com.example.projectmanagement.dto.RiskCategoryResponse;

import java.util.List;

public interface RiskCategoryService {

    RiskCategoryResponse createCategory(RiskCategoryCreateRequest request);

    List<RiskCategoryResponse> getAllCategories();

    RiskCategoryResponse getCategoryById(Long id);

    RiskCategoryResponse updateCategory(Long id, RiskCategoryCreateRequest request);

    void deleteCategory(Long id);
}
