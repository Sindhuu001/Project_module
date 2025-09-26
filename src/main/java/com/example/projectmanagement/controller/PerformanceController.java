package com.example.projectmanagement.controller;
// hello by ruch
import com.example.projectmanagement.dto.EmployeePerformanceDto;
import com.example.projectmanagement.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<EmployeePerformanceDto>> getAllEmployeePerformance() {
        List<EmployeePerformanceDto> result = performanceService.getAllEmployeePerformance();
        return ResponseEntity.ok(result);
    }
}
