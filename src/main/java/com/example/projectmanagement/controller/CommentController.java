package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.CommentDto;
import com.example.projectmanagement.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


@RestController
@CrossOrigin
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // ----------------- Add Comments -----------------

    @PostMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<CommentDto> addCommentToTask(
            @PathVariable Long taskId,
            @RequestBody CommentDto commentDto) {
        CommentDto saved = commentService.addCommentToTask(taskId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/story/{storyId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<CommentDto> addCommentToStory(
            @PathVariable Long storyId,
            @RequestBody CommentDto commentDto) {
        CommentDto saved = commentService.addCommentToStory(storyId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/epic/{epicId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<CommentDto> addCommentToEpic(
            @PathVariable Long epicId,
            @RequestBody CommentDto commentDto) {
        CommentDto saved = commentService.addCommentToEpic(epicId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ----------------- Get Comments -----------------

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<CommentDto>> getCommentsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId));
    }

    @GetMapping("/story/{storyId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<CommentDto>> getCommentsByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(commentService.getCommentsByStoryId(storyId));
    }

    @GetMapping("/epic/{epicId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<CommentDto>> getCommentsByEpic(@PathVariable Long epicId) {
        return ResponseEntity.ok(commentService.getCommentsByEpicId(epicId));
    }

    // ----------------- Replies -----------------

    @GetMapping("/replies/{parentId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getRepliesByParentId(parentId));
    }

    // ----------------- Delete -----------------

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
