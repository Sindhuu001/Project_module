package com.example.projectmanagement.dto.testing;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BugFilterRequest {
    // getters and setters omitted for brevity - generate them or use Lombok
    private String status;
    private String priority;
    private String severity;
    private Long assignedTo;
    private Long reporter;
    private Long projectId;
    private Long runId;
    private Long testCaseId;
    private Long scenarioId;
    private Long storyId;
    private String fromDate;
    private String toDate;
    private int page = 0;
    private int size = 20;
    private String sort;

}


