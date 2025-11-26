package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.enums.BugStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByRunCaseId(Long runCaseId);

    List<Bug> findByRunCaseStepId(Long runCaseStepId);

    // find runCase-linked bugs in a particular status set
    List<Bug> findByRunCaseIdAndStatusIn(Long runCaseId, List<BugStatus> statuses);

    // optional: find bugs by run id
    List<Bug> findByTestRunId(Long runId);
}
