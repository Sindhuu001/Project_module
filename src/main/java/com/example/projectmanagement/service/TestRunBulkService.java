package com.example.projectmanagement.service;
import com.example.projectmanagement.dto.testing.BulkAssignRequest;
import com.example.projectmanagement.dto.testing.BulkExecutionRequest;
import com.example.projectmanagement.dto.testing.CloneRunRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;

public interface TestRunBulkService {

    void bulkAssign(Long runId, BulkAssignRequest req, Long userId);

    void bulkPass(Long runId, BulkExecutionRequest req, Long userId);

    void bulkSkip(Long runId, BulkExecutionRequest req, Long userId);

    TestRunSummaryResponse cloneNextRun(Long cycleId, CloneRunRequest req, Long userId);
}
