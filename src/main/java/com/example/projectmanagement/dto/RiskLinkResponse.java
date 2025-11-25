package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.RiskLink.LinkedType;
import lombok.Data;

@Data
public class RiskLinkResponse {
    private Long id;
    private Long riskId;
    private LinkedType linkedType;
    private Long linkedId;
}
