package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestPriority;
import com.example.projectmanagement.enums.TestScenarioStatus;
import jakarta.validation.constraints.Size;

public record TestScenarioUpdateRequest(
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,
        
        String description,
        
        TestPriority priority,
        
        TestScenarioStatus status
) {}