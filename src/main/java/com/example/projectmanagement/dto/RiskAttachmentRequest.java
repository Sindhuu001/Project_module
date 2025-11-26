package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class RiskAttachmentRequest {
    private Long riskId;
    private String fileName;
    private String fileType;
    private byte[] fileData; // can also use MultipartFile in controller for upload
}
