package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.CommentDto;
import com.example.projectmanagement.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // ----------------- Add Comments -----------------

    @PostMapping("/task/{taskId}")
    public ResponseEntity<CommentDto> addCommentToTask(
            @PathVariable Long taskId,
            @RequestBody CommentDto commentDto
    ) {
        CommentDto saved = commentService.addCommentToTask(taskId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/story/{storyId}")
    public ResponseEntity<CommentDto> addCommentToStory(
            @PathVariable Long storyId,
            @RequestBody CommentDto commentDto
    ) {
        CommentDto saved = commentService.addCommentToStory(storyId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/epic/{epicId}")
    public ResponseEntity<CommentDto> addCommentToEpic(
            @PathVariable Long epicId,
            @RequestBody CommentDto commentDto
    ) {
        CommentDto saved = commentService.addCommentToEpic(epicId, commentDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ----------------- Get Comments -----------------

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDto>> getCommentsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId));
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<CommentDto>> getCommentsByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(commentService.getCommentsByStoryId(storyId));
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEpic(@PathVariable Long epicId) {
        return ResponseEntity.ok(commentService.getCommentsByEpicId(epicId));
    }

    // ----------------- Replies -----------------

    @GetMapping("/replies/{parentId}")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getRepliesByParentId(parentId));
    }

    // ----------------- Delete -----------------

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
