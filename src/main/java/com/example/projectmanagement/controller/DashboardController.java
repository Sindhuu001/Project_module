package com.example.projectmanagement.controller;
import com.example.projectmanagement.dto.DashboardSummaryDto;
import com.example.projectmanagement.service.DashboardService;
//import com.example.projectmanagement.service.ReminderService;

import java.util.Map;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/api/dashboard")
//@CrossOrigin
public class DashboardController {
// @Autowired
// private ReminderService reminderService;

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary/")
   @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/reminders/")
    public ResponseEntity<Map<String, Long>> getReminders() {
        return ResponseEntity.ok(dashboardService.getReminders());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getDashboardData(@PathVariable Long userId) {
        Map<String, Object> summary = dashboardService.getDashboardData(userId);
        return ResponseEntity.ok(summary);
    }




}