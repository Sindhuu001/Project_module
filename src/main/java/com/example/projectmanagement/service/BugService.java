package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.BugCreateRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.dto.testing.BugStatusUpdateRequest;

public interface BugService {

    BugResponse createBug(BugCreateRequest req, Long reporterId);

    BugResponse updateBugStatus(Long bugId, BugStatusUpdateRequest req, Long userId);

    /**
     * Called when a TestRunCase is passed successfully (retest passed).
     * Will auto-close bugs that are READY_FOR_RETEST (or FIXED if your flow sets FIXED directly)
     * and are linked to this run-case or its steps.
     */
    void handleCasePassed(Long runCaseId, Long currentUserId);
    /**
     * Called when a TestRunCase fails during execution / retest.
     * Will auto-reopen bugs that are in FIXED/READY_FOR_RETEST states and are linked to this run-case.
     *
     * @param runCaseId run-case that failed (execution context)
     * @param runCaseStepId optional, the specific step id that failed (nullable)
     * @param currentUserId user who executed the test (tester)
     */
    void handleCaseFailed(Long runCaseId, Long runCaseStepId, Long currentUserId);
}
