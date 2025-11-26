package com.example.projectmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiskAttachmentResponse {
    private Long id;
    private Long riskId;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadedAt;
}
