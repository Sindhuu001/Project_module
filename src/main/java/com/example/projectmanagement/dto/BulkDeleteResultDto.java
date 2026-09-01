package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkDeleteResultDto {
    private int totalDeleted;
    private int totalNotFound;
    private List<Long> deletedIds;
    private List<Long> notFoundIds;
    private String message;
}
