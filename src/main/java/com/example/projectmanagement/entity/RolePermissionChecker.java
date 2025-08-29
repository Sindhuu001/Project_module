package com.example.projectmanagement.entity;

import com.example.projectmanagement.entity.User.UserRole;

public class RolePermissionChecker {

    public static boolean canUpdateTask(UserRole role) {
        return role == UserRole.DEVELOPER || role == UserRole.Admin;
    }

    public static boolean canStartSprint(UserRole role) {
        return role == UserRole.PRODUCT_OWNER || role == UserRole.SCRUM_MASTER || role == UserRole.Admin;
    }

    public static boolean canDeleteSprint(UserRole role) {
        return role == UserRole.Admin;
    }

    public static boolean canCreateSprint(UserRole role) {
        return role == UserRole.PRODUCT_OWNER || role == UserRole.SCRUM_MASTER || role == UserRole.Admin;
    }

    // Add more methods as needed
}