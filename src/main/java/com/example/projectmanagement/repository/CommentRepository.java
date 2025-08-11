package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Fetch top-level comments for Task, Story, Epic
    List<Comment> findByTaskId(Long taskId);
    List<Comment> findByStoryId(Long storyId);
    List<Comment> findByEpicId(Long epicId);

    // Fetch replies to a specific comment
    List<Comment> findByParentId(Long parentId);
}
