package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.RiskLinkRequest;
import com.example.projectmanagement.dto.RiskLinkResponse;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskLink;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.service.RiskLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskLinkServiceImpl implements RiskLinkService {

    @Autowired
    private RiskLinkRepository linkRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private EpicRepository epicRepository;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private BugRepository bugRepository;
//    @Autowired
//    private ReleaseRepository releaseRepository;

    @Override
    public RiskLinkResponse createLink(RiskLinkRequest request) {
        Risk risk = riskRepository.findById(request.getRiskId())
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        validateLinkedId(request.getLinkedType(), request.getLinkedId());

        // Check for duplicate
        boolean exists = linkRepository.existsByRiskAndLinkedTypeAndLinkedId(
                risk, request.getLinkedType(), request.getLinkedId());
        if (exists) {
            throw new IllegalArgumentException("This risk is already linked to the selected entity");
        }

        RiskLink link = new RiskLink();
        link.setRisk(risk);
        link.setLinkedType(request.getLinkedType());
        link.setLinkedId(request.getLinkedId());

        return toResponse(linkRepository.save(link));
    }


    @Override
    public RiskLinkResponse updateLink(Long id, RiskLinkRequest request) {
        RiskLink link = linkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Risk link not found"));

        validateLinkedId(request.getLinkedType(), request.getLinkedId());

        link.setLinkedType(request.getLinkedType());
        link.setLinkedId(request.getLinkedId());

        return toResponse(linkRepository.save(link));
    }

    @Override
    public void deleteLink(Long id) {
        RiskLink link = linkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Risk link not found"));
        linkRepository.delete(link);
    }

    @Override
    public List<RiskLinkResponse> getLinksByRiskId(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        return linkRepository.findByRisk(risk)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Helper to validate linked entity exists
    private void validateLinkedId(RiskLink.LinkedType type, Long id) {
        switch (type) {
            case Epic -> epicRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Epic not found with id " + id));
            case Sprint -> sprintRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sprint not found with id " + id));
            case Story -> storyRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Story not found with id " + id));
            case Task -> taskRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found with id " + id));
            case Bug -> bugRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bug not found with id " + id));
            default -> throw new IllegalArgumentException("Unsupported linked type");
        }
    }

    private RiskLinkResponse toResponse(RiskLink link) {
        RiskLinkResponse response = new RiskLinkResponse();
        response.setId(link.getId());
        response.setRiskId(link.getRisk().getId());
        response.setLinkedType(link.getLinkedType());
        response.setLinkedId(link.getLinkedId());
        return response;
    }
}
