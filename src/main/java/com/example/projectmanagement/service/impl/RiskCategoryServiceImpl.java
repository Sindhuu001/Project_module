package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.RiskCategoryCreateRequest;
import com.example.projectmanagement.dto.RiskCategoryResponse;
import com.example.projectmanagement.entity.RiskCategory;
import com.example.projectmanagement.repository.RiskCategoryRepository;
import com.example.projectmanagement.service.RiskCategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskCategoryServiceImpl implements RiskCategoryService {

    private final RiskCategoryRepository riskCategoryRepository;

    @Override
    public RiskCategoryResponse createCategory(RiskCategoryCreateRequest request) {

        // prevent duplicates
        riskCategoryRepository.findByName(request.getName()).ifPresent(rc -> {
            throw new RuntimeException("Risk category with this name already exists.");
        });

        RiskCategory category = new RiskCategory(
                request.getName(),
                request.getDescription()
        );

        RiskCategory saved = riskCategoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    public List<RiskCategoryResponse> getAllCategories() {
        return riskCategoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RiskCategoryResponse getCategoryById(Long id) {
        RiskCategory category = riskCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk category not found"));

        return mapToResponse(category);
    }

    @Override
    public RiskCategoryResponse updateCategory(Long id, RiskCategoryCreateRequest request) {
        RiskCategory category = riskCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        RiskCategory updated = riskCategoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        RiskCategory category = riskCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk category not found"));

        riskCategoryRepository.delete(category);
    }

    // 🔥 Mapper method inside service — NO separate class needed
    private RiskCategoryResponse mapToResponse(RiskCategory entity) {
        RiskCategoryResponse dto = new RiskCategoryResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
