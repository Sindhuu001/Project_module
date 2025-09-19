package com.example.projectmanagement.entity;

import java.util.List;

import lombok.Data;

@Data
public class RolePermissionChecker {

    public static boolean canUpdateTask(List<String> roles) {
        return roles.contains("General") || roles.contains("Admin");
    }

    public static boolean canStartSprint(List<String> roles) {
        return roles.contains("Manager");
    }

    public static boolean canDeleteSprint(List<String> roles) {
        return roles.contains("Manager") || roles.contains("Admin");
    }

    public static boolean canCreateSprint(List<String> roles) {
        return roles.contains("Manager") || roles.contains("Admin");
    }

    // Add more methods as needed
}