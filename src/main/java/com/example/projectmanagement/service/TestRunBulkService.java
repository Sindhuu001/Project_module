package com.example.projectmanagement.service;
import com.example.projectmanagement.dto.testing.*;

public interface TestRunBulkService {

    void bulkAssign(Long runId, BulkAssignRequest req, Long userId);

    void bulkPass(Long runId, BulkExecutionRequest req, Long userId);

    void bulkSkip(Long runId, BulkExecutionRequest req, Long userId);

    TestRunSummaryResponse cloneNextRun(Long cycleId, CloneRunRequest req, Long userId);

}
