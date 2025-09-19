package com.example.projectmanagement.dto;

import java.util.List;

import lombok.Data;
@Data
public class EmployeePerformanceDto {
    private String employeeName;
    private String employeeEmail;
    private List<String> projectNames;
    private int totalTasks;
    private int tasksInProgress;
    private int tasksCompleted;
    private int tasksOverdue;

    // Getters and Setters
private List<EpicWithStoriesDto> epics;

public List<EpicWithStoriesDto> getEpics() {
    return epics;
}

public void setEpics(List<EpicWithStoriesDto> epics) {
    this.epics = epics;
}

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getTasksInProgress() {
        return tasksInProgress;
    }

    public void setTasksInProgress(int tasksInProgress) {
        this.tasksInProgress = tasksInProgress;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getTasksOverdue() {
        return tasksOverdue;
    }

    public void setTasksOverdue(int tasksOverdue) {
        this.tasksOverdue = tasksOverdue;
    }
}
