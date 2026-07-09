package com.example.projectmanagement.audit.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectmanagement.audit.base.AuditTrail.AuditEntityType;
import com.example.projectmanagement.audit.dynamic.DynamicAuditService;

@RestController
@RequestMapping("api/activity")
public class ActivityController {

    @Autowired
    private DynamicAuditService dynamicAuditService;

    // ── All activities per entity type ──────────────────────────────────────

    @GetMapping("epic")
    public List<AuditActivityDto> getEpicActivities() {
        return dynamicAuditService.getActivities(AuditEntityType.EPIC);
    }

    @GetMapping("story")
    public List<AuditActivityDto> getStoryActivities() {
        return dynamicAuditService.getActivities(AuditEntityType.STORY);
    }

    @GetMapping("task")
    public List<AuditActivityDto> getTaskActivities() {
        return dynamicAuditService.getActivities(AuditEntityType.TASK);
    }

    @GetMapping("sprint")
    public List<AuditActivityDto> getSprintActivities() {
        return dynamicAuditService.getActivities(AuditEntityType.SPRINT);
    }

    // ── Comment activities ───────────────────────────────────────────────────

    @GetMapping("comment")
    public List<AuditActivityDto> getCommentActivities() {
        return dynamicAuditService.getActivities(AuditEntityType.COMMENT);
    }

    @GetMapping("comment/project/{projectId}")
    public List<AuditActivityDto> getCommentActivitiesByProject(@PathVariable Long projectId) {
        return dynamicAuditService.getCommentActivitiesByProjectId(projectId);
    }

    // ── Activities filtered by project ID ───────────────────────────────────

    @GetMapping("epic/project/{projectId}")
    public List<AuditActivityDto> getEpicActivitiesByProject(@PathVariable Long projectId) {
        return dynamicAuditService.getActivitiesByProjectId(AuditEntityType.EPIC, projectId);
    }

    @GetMapping("story/project/{projectId}")
    public List<AuditActivityDto> getStoryActivitiesByProject(@PathVariable Long projectId) {
        return dynamicAuditService.getActivitiesByProjectId(AuditEntityType.STORY, projectId);
    }

    @GetMapping("task/project/{projectId}")
    public List<AuditActivityDto> getTaskActivitiesByProject(@PathVariable Long projectId) {
        return dynamicAuditService.getActivitiesByProjectId(AuditEntityType.TASK, projectId);
    }

    @GetMapping("sprint/project/{projectId}")
    public List<AuditActivityDto> getSprintActivitiesByProject(@PathVariable Long projectId) {
        return dynamicAuditService.getActivitiesByProjectId(AuditEntityType.SPRINT, projectId);
    }
}
