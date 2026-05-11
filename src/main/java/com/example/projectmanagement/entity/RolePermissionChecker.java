package com.example.projectmanagement.entity;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class RolePermissionChecker {

    private static List<String> normalizeRoles(List<String> roles) {
        return roles.stream()
                .map(role -> role.replace(" ", "_").toUpperCase())
                .collect(Collectors.toList());
    }

    public static boolean canUpdateTask(List<String> roles) {
        List<String> normalized = normalizeRoles(roles);
        return normalized.contains("GENERAL") || normalized.contains("PROJECT_MANAGER") || normalized.contains("ADMIN");
    }

    public static boolean canStartSprint(List<String> roles) {
        List<String> normalized = normalizeRoles(roles);
        return normalized.contains("PROJECT_MANAGER");
    }

    public static boolean canDeleteSprint(List<String> roles) {
        List<String> normalized = normalizeRoles(roles);
        return normalized.contains("PROJECT_MANAGER") || normalized.contains("ADMIN");
    }

    public static boolean canCreateSprint(List<String> roles) {
        List<String> normalized = normalizeRoles(roles);
        return normalized.contains("PROJECT_MANAGER") || normalized.contains("ADMIN");
    }

    // Add more methods as needed
}