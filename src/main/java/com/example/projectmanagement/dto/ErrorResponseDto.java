package com.example.projectmanagement.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private String code;
    private String message;
    private ErrorDataDto data;

    @Data
    @AllArgsConstructor
    public static class ErrorDataDto {
        private List<String> pendingTasks;
        private List<String> pendingStories;
    }
}