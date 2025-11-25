package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskLinkRequest;
import com.example.projectmanagement.dto.RiskLinkResponse;
import com.example.projectmanagement.service.RiskLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-links")
public class RiskLinkController {

    @Autowired
    private RiskLinkService linkService;

    @PostMapping
    public ResponseEntity<RiskLinkResponse> create(@RequestBody RiskLinkRequest request) {
        return ResponseEntity.ok(linkService.createLink(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RiskLinkResponse> update(@PathVariable Long id, @RequestBody RiskLinkRequest request) {
        return ResponseEntity.ok(linkService.updateLink(id, request));
    }

    @GetMapping("/risk/{riskId}")
    public ResponseEntity<List<RiskLinkResponse>> getByRisk(@PathVariable Long riskId) {
        return ResponseEntity.ok(linkService.getLinksByRiskId(riskId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        linkService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }
}
