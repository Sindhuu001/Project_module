package com.example.projectmanagement.enums;

public enum TestRunStatus {
    CREATED,        // run created, no execution yet
    IN_PROGRESS,    // some test cases being executed
    COMPLETED,      // all cases finished
    CANCELLED       // run stopped
}