package com.example.projectmanagement.audit.base;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditActivityDto {
    private Long userId;
    private String entityType;
    private String entityId;
    private String operation;
    private String field;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}
