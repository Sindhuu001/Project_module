package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class RiskRequest {
    private Long projectId;
    private Long ownerId;
    private Long reporterId;
    private Long categoryId;
    private Long statusId;
    private String title;
    private String description;
    private Byte probability;
    private Byte impact;
    private String triggers;
}
