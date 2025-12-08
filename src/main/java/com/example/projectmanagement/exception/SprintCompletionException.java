package com.example.projectmanagement.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class SprintCompletionException extends RuntimeException {
    private final List<String> pendingTasks;
    private final List<String> pendingStories;

    public SprintCompletionException(List<String> pendingTasks, List<String> pendingStories) {
        super("Cannot complete sprint. Please resolve pending items.");
        this.pendingTasks = pendingTasks;
        this.pendingStories = pendingStories;
    }
}