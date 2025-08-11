package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.CommentDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final StoryRepository storyRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;

    // ------------- Add Comment to TASK -------------
    public CommentDto addCommentToTask(Long taskId, CommentDto dto) {
        Task task = getTaskById(taskId);
        User user = getUserById(dto.getUserId());

        Comment comment = buildComment(user, dto.getContent(), dto.getParentId());
        comment.setTask(task);

        return mapToDto(commentRepository.save(comment));
    }

    // ------------- Add Comment to STORY -------------
    public CommentDto addCommentToStory(Long storyId, CommentDto dto) {
        Story story = getStoryById(storyId);
        User user = getUserById(dto.getUserId());

        Comment comment = buildComment(user, dto.getContent(), dto.getParentId());
        comment.setStory(story);

        return mapToDto(commentRepository.save(comment));
    }

    // ------------- Add Comment to EPIC -------------
    public CommentDto addCommentToEpic(Long epicId, CommentDto dto) {
        Epic epic = getEpicById(epicId);
        User user = getUserById(dto.getUserId());

        Comment comment = buildComment(user, dto.getContent(), dto.getParentId());
        comment.setEpic(epic);

        return mapToDto(commentRepository.save(comment));
    }

    // ------------- Fetch Comments by Entities -------------
    public List<CommentDto> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskId(taskId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByStoryId(Long storyId) {
        return commentRepository.findByStoryId(storyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByEpicId(Long epicId) {
        return commentRepository.findByEpicId(epicId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ------------- Fetch Replies to a Comment -------------
    public List<CommentDto> getRepliesByParentId(Long parentId) {
        return commentRepository.findByParentId(parentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ------------- Delete Comment -------------
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: cannot delete comment by another user");
        }

        commentRepository.delete(comment);
    }

    // ------------- Helpers -------------
    private Comment buildComment(User user, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with ID: " + parentId));
            comment.setParent(parent);
        }

        return comment;
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    private Story getStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));
    }

    private Epic getEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .orElseThrow(() -> new RuntimeException("Epic not found with ID: " + epicId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }
        return dto;
    }
}
