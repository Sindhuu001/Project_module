package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.service.EpicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin
@RestController
@RequestMapping("/api/epics")

public class EpicController {

    @Autowired
    private EpicService epicService;

    // Create new Epic
    @PostMapping
    public ResponseEntity<EpicDto> createEpic(@RequestBody EpicDto epicDto) {
        EpicDto createdEpic = epicService.createEpic(epicDto);
        return ResponseEntity.ok(createdEpic);
    }

    // Get all epics
    @GetMapping
    public ResponseEntity<List<EpicDto>> getAllEpics() {
        return ResponseEntity.ok(epicService.getAllEpics());
    }

    // Get epic by ID
    @GetMapping("/{id}")
    public ResponseEntity<EpicDto> getEpicById(@PathVariable Long id) {
        EpicDto epicDto = epicService.getEpicById(id);
        if (epicDto != null) {
            return ResponseEntity.ok(epicDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update epic
    @PutMapping("/{id}")
    public ResponseEntity<EpicDto> updateEpic(@PathVariable Long id, @RequestBody EpicDto epicDto) {
        EpicDto updatedEpic = epicService.updateEpic(id, epicDto);
        if (updatedEpic != null) {
            return ResponseEntity.ok(updatedEpic);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete epic
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpic(@PathVariable Long id) {
        boolean deleted = epicService.deleteEpic(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get epics by project ID
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<EpicDto>> getEpicsByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(epicService.getEpicsByProjectId(projectId));
    }

    // // Get epics by organization ID
    // @GetMapping("/organization/{organizationId}")
    // public ResponseEntity<List<EpicDto>> getEpicsByOrganizationId(@PathVariable Long organizationId) {
    //     return ResponseEntity.ok(epicService.getEpicsByOrganizationId(organizationId));
    // }
}
