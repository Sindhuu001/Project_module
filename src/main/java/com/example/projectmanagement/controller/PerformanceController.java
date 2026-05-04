package com.example.projectmanagement.controller;

// hello by ruch
import com.example.projectmanagement.dto.EmployeePerformanceDto;
import com.example.projectmanagement.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/test")
    public String test() {
        return "Controller is working";
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<EmployeePerformanceDto>> getAllEmployeePerformance() {
        List<EmployeePerformanceDto> result = performanceService.getAllEmployeePerformance();
        return ResponseEntity.ok(result);
    }
}
