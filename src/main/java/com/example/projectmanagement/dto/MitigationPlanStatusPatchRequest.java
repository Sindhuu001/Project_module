package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class MitigationPlanStatusPatchRequest {
    private Boolean used;       // mark if mitigation applied
    private Boolean effective;  // mark if mitigation was effective
}
