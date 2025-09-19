package com.example.projectmanagement.dto;

import java.util.List;

import lombok.Data;
@Data
public class StoryWithTasksDto {
    private Long storyId;
    private String storyTitle;
    private List<String> taskTitles;

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public List<String> getTaskTitles() {
        return taskTitles;
    }

    public void setTaskTitles(List<String> taskTitles) {
        this.taskTitles = taskTitles;
    }
}
