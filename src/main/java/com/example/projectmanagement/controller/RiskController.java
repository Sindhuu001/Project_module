package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskRequest;
import com.example.projectmanagement.dto.RiskResponse;
import com.example.projectmanagement.dto.RiskStatusUpdateRequest;
import com.example.projectmanagement.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    @Autowired
    private RiskService riskService;

    @PostMapping
    public RiskResponse createRisk(@RequestBody RiskRequest request) {
        return riskService.createRisk(request);
    }

    @GetMapping("/project/{projectId}")
    public List<RiskResponse> getRisksByProject(@PathVariable Long projectId) {
        return riskService.getRisksByProject(projectId);
    }

    @GetMapping("/{id}")
    public RiskResponse getRisk(@PathVariable Long id) {
        return riskService.getRiskById(id);
    }

    @PutMapping("/{id}")
    public RiskResponse updateRisk(@PathVariable Long id, @RequestBody RiskRequest request) {
        return riskService.updateRisk(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRisk(@PathVariable Long id) {
        riskService.deleteRisk(id);
    }

    @PatchMapping("/{id}/status")
    public RiskResponse updateRiskStatus(@PathVariable Long id, @RequestBody RiskStatusUpdateRequest request) {
        return riskService.updateStatus(id, request);
    }
}
