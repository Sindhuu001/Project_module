package com.example.projectmanagement.enums;

public enum BugStatus {
    NEW("New"),
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    FIXED("Fixed"),
    READY_FOR_RETEST("Ready for Retest"),
    CLOSED("Closed"),
    REOPENED("Reopened"),
    WON_T_FIX("Won't Fix"),
    DUPLICATE("Duplicate"),
    CANNOT_REPRODUCE("Cannot Reproduce");

    private final String displayName;

    BugStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
