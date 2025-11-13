package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @PostMapping("/{projectId}")
    public StatusDto addCustomStatus(
            @PathVariable Long projectId,
            @RequestParam String name,
            @RequestParam(defaultValue = "false") boolean isBug,
            @RequestParam(defaultValue = "10") int sortOrder
    ) {
        return statusService.addCustomStatusDto(projectId, name, isBug, sortOrder);
    }

    @PatchMapping("/{statusId}/toggle")
    public String toggleStatus(@PathVariable Long statusId, @RequestParam boolean enabled) {
        statusService.toggleStatus(statusId, enabled);
        return "Status toggled successfully";
    }

    @PostMapping("/{projectId}/reorder")
    public String reorderStatuses(@PathVariable Long projectId, @RequestBody List<Long> orderedIds) {
        statusService.reorderStatuses(projectId, orderedIds);
        return "Statuses reordered successfully";
    }

    @DeleteMapping("/{statusId}")
    public String deleteStatus(@PathVariable Long statusId) {
        statusService.deleteStatus(statusId);
        return "Status deleted successfully";
    }

    @GetMapping("/{projectId}")
    public List<StatusDto> getStatuses(@PathVariable Long projectId) {
        return statusService.getAllByProjectDto(projectId);
    }

    @GetMapping("/{projectId}/active")
    public List<StatusDto> getActiveStatuses(@PathVariable Long projectId) {
        return statusService.getActiveStatusesByProject(projectId);
    }

    @GetMapping("/{projectId}/active-bugs")
    public List<StatusDto> getActiveBugStatuses(@PathVariable Long projectId) {
        return statusService.getActiveBugStatusesByProject(projectId);
    }
}
