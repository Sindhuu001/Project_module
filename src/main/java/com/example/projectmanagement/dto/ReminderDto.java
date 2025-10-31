package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class ReminderDto {
    private long tasksDueSoon;
    private long projectsWithoutOwner;
    private long sprintsEndingSoon;

    public ReminderDto() {}

    public ReminderDto(long tasksDueSoon, long projectsWithoutOwner, long sprintsEndingSoon) {
        this.tasksDueSoon = tasksDueSoon;
        this.projectsWithoutOwner = projectsWithoutOwner;
        this.sprintsEndingSoon = sprintsEndingSoon;
    }

    public long getTasksDueSoon() {
        return tasksDueSoon;
    }

    public void setTasksDueSoon(long tasksDueSoon) {
        this.tasksDueSoon = tasksDueSoon;
    }

    public long getProjectsWithoutOwner() {
        return projectsWithoutOwner;
    }

    public void setProjectsWithoutOwner(long projectsWithoutOwner) {
        this.projectsWithoutOwner = projectsWithoutOwner;
    }

    public long getSprintsEndingSoon() {
        return sprintsEndingSoon;
    }

    public void setSprintsEndingSoon(long sprintsEndingSoon) {
        this.sprintsEndingSoon = sprintsEndingSoon;
    }
}
