package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.BugFilterRequest;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.entity.testing.TestScenario;
import com.example.projectmanagement.entity.testing.TestStory;
import com.example.projectmanagement.enums.BugPriority;
import com.example.projectmanagement.enums.BugSeverity;
import com.example.projectmanagement.enums.BugStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BugSpecification {

    public static Specification<Bug> filter(BugFilterRequest req) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (req.getStatus() != null && !req.getStatus().isBlank()) {
                try {
                    BugStatus st = BugStatus.valueOf(req.getStatus().trim().toUpperCase(Locale.ROOT));
                    preds.add(cb.equal(root.get("status"), st));
                } catch (Exception ignored) {}
            }
            if (req.getPriority() != null && !req.getPriority().isBlank()) {
                try {
                    BugPriority p = BugPriority.valueOf(req.getPriority().trim().toUpperCase(Locale.ROOT));
                    preds.add(cb.equal(root.get("priority"), p));
                } catch (Exception ignored) {}
            }
            if (req.getSeverity() != null && !req.getSeverity().isBlank()) {
                try {
                    BugSeverity s = BugSeverity.valueOf(req.getSeverity().trim().toUpperCase(Locale.ROOT));
                    preds.add(cb.equal(root.get("severity"), s));
                } catch (Exception ignored) {}
            }
            if (req.getAssignedTo() != null) {
                preds.add(cb.equal(root.get("assignedTo"), req.getAssignedTo()));
            }
            if (req.getReporter() != null) {
                preds.add(cb.equal(root.get("reporter"), req.getReporter()));
            }
            if (req.getProjectId() != null) {
                preds.add(cb.equal(root.get("project").get("id"), req.getProjectId()));
            }
            if (req.getRunId() != null) {
                preds.add(cb.equal(root.get("testRun").get("id"), req.getRunId()));
            }
            if (req.getTestCaseId() != null) {
                preds.add(cb.equal(root.get("testCase").get("id"), req.getTestCaseId()));
            }
            if (req.getScenarioId() != null) {
                Join<Bug, TestScenario> scenarioJoin = root.join("testScenario");
                preds.add(cb.equal(scenarioJoin.get("id"), req.getScenarioId()));
            }
            if (req.getStoryId() != null) {
                Join<Bug, TestStory> storyJoin = root.join("testStory");
                preds.add(cb.equal(storyJoin.get("id"), req.getStoryId()));
            }

            DateTimeFormatter f = DateTimeFormatter.ISO_DATE_TIME;
            if (req.getFromDate() != null && !req.getFromDate().isBlank()) {
                try {
                    LocalDateTime from = LocalDateTime.parse(req.getFromDate(), f);
                    preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                } catch (Exception ignored) {}
            }
            if (req.getToDate() != null && !req.getToDate().isBlank()) {
                try {
                    LocalDateTime to = LocalDateTime.parse(req.getToDate(), f);
                    preds.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
                } catch (Exception ignored) {}
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
