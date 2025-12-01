package com.example.projectmanagement.dto.testing;

public record ScenarioReportResponse(Long scenarioId, String title,
                                     int totalCases, int passed, int failed, int skipped, int openBugs) {}

