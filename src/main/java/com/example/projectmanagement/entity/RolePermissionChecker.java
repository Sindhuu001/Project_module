package com.example.projectmanagement.entity;

import java.util.List;

import lombok.Data;

@Data
public class RolePermissionChecker {

    public static boolean canUpdateTask(List<String> roles) {
        return roles.contains("General") || roles.contains("PROJECT_MANAGER") || roles.contains("Admin");
    }

    public static boolean canStartSprint(List<String> roles) {
        return roles.contains("PROJECT_MANAGER");
    }

    public static boolean canDeleteSprint(List<String> roles) {
        return roles.contains("PROJECT_MANAGER") || roles.contains("Admin");
    }

    public static boolean canCreateSprint(List<String> roles) {
        return roles.contains("PROJECT_MANAGER") || roles.contains("Admin");
    }

    // Add more methods as needed
}