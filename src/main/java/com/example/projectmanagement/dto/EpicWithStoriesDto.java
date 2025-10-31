package com.example.projectmanagement.dto;

import java.util.List;

import lombok.Data;
@Data
public class EpicWithStoriesDto {
    private Long epicId;
    private String epicName;
    private List<StoryWithTasksDto> stories;

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public String getEpicName() {
        return epicName;
    }

    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }

    public List<StoryWithTasksDto> getStories() {
        return stories;
    }

    public void setStories(List<StoryWithTasksDto> stories) {
        this.stories = stories;
    }
}
