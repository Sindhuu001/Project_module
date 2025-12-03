package com.example.projectmanagement.dto.testing;

public record CommentResponse(Long id, String comment, Long commentedBy, String createdAt, Long runCaseId, Long runCaseStepId) {}

