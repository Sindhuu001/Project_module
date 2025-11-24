package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SprintPopupResponse {
    private Long sprintId;
    private String sprintName;
    private boolean isEndingSoon;
    private boolean hasUnfinishedTasks;
    private boolean shouldShowPopup;
}
