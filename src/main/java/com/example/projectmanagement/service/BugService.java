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
}
