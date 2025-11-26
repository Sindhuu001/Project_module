package com.example.projectmanagement.dto.testing;

public record CloneRunRequest(
        Boolean includeFailedOnly,   // default true
        Boolean copyAssignee         // default false
) {}

