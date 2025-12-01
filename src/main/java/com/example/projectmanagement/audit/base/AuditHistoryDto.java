package com.example.projectmanagement.audit.base;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditHistoryDto {
    private String userName;
    private String action;
    private String field;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private String operation;
    private String oldData;
    private String newData;
    

    
}
