package com.example.projectmanagement.ExternalDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectTasksDto {
    private Long projectId;
    private String projectName;
    private List<TaskDto> tasks;

    @Data
    @AllArgsConstructor
    public static class TaskDto {
        private Long taskId;
        private String taskName;
    }
}
