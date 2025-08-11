package com.example.projectmanagement.dto;

import java.time.LocalDateTime;

public class CommentDto {
    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private String text;
    private Long parentId; // For replies to comments

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Default constructor
    public CommentDto() {}

    // Constructor with text
    public CommentDto(String text) {
        this.text = text;
    }

    // Getter
    public String getText() {
        return text;
    }

    // Setter
    public void setText(String text) {
        this.text = text;
    }
    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
