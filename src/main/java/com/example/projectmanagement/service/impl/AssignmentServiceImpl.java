package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.*;
import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.enums.AssignmentObjectType;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.AssignmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final TestRunRepository testRunRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRunCaseRepository testRunCaseRepository;

    @Override
    public AssignmentValidateResponse validateAssignment(AssignmentValidateRequest req) {
        // 1) Validate run exists
        TestRun run = testRunRepository.findById(req.runId())
                .orElseThrow(() -> new EntityNotFoundException("TestRun not found: " + req.runId()));

        // 2) gather case ids depending on object type
        List<Long> caseIds = fetchCaseIdsForObject(req.objectType(), req.objectId());

        // total
        int total = caseIds.size();
        if (total == 0) {
            return new AssignmentValidateResponse(false, 0, 0, 0, 0, Collections.emptyList());
        }

        // 3) find existing run-case entries in a single query
        List<TestRunCase> existing = testRunCaseRepository.findByRunIdAndTestCaseIdIn(run.getId(), caseIds);

        // prepare quick lookup
        Map<Long, TestRunCase> byCaseId = existing.stream().collect(Collectors.toMap(
                rc -> rc.getTestCase().getId(),
                rc -> rc
        ));

        int alreadyInRun = existing.size();
        int alreadyAssigned = (int) existing.stream().filter(rc -> rc.getAssigneeId() != null).count();
        int unassigned = total - alreadyAssigned;

        // prepare conflicts list: cases already assigned (have assignee)
        List<AssignmentConflictItem> conflicts = existing.stream()
                .filter(rc -> rc.getAssigneeId() != null)
                .map(rc -> new AssignmentConflictItem(
                        rc.getTestCase().getId(),
                        rc.getId(),
                        rc.getAssigneeId()
                ))
                .collect(Collectors.toList());

        boolean conflict = !conflicts.isEmpty();

        return new AssignmentValidateResponse(
                conflict,
                total,
                alreadyInRun,
                alreadyAssigned,
                unassigned,
                conflicts
        );
    }

    @Override
    @Transactional
    public AssignmentApplyResponse applyAssignment(AssignmentApplyRequest req, Long currentUserId) {
        TestRun run = testRunRepository.findById(req.runId())
                .orElseThrow(() -> new EntityNotFoundException("TestRun not found: " + req.runId()));

        List<Long> caseIds = fetchCaseIdsForObject(req.objectType(), req.objectId());

        if (caseIds.isEmpty()) {
            return new AssignmentApplyResponse(0, 0, Collections.emptyList());
        }

        // load existing run-case in single query
        List<TestRunCase> existing = testRunCaseRepository.findByRunIdAndTestCaseIdIn(run.getId(), caseIds);
        Map<Long, TestRunCase> existingMap = existing.stream()
                .collect(Collectors.toMap(rc -> rc.getTestCase().getId(), rc -> rc));

        List<TestRunCase> toCreate = new ArrayList<>();
        List<TestRunCase> toUpdate = new ArrayList<>();
        List<Long> createdIds = new ArrayList<>();

        Long assignTo = req.assignTo(); // may be null

        // iterate all case ids once (keeps memory small)
        for (Long caseId : caseIds) {
            TestRunCase present = existingMap.get(caseId);

            if (present == null) {
                // create new TestRunCase
                TestRunCase newRc = TestRunCase.builder()
                        .run(run)
                        .testCase(com.example.projectmanagement.entity.testing.TestCase.builder().id(caseId).build())
                        .assigneeId(assignTo)
                        .status(TestRunCaseStatus.NOT_STARTED)
                        .createdAt(LocalDateTime.now())
                        .build();
                toCreate.add(newRc);
            } else {
                // exists
                boolean hasAssignee = present.getAssigneeId() != null;
                switch (req.action()) {
                    case REASSIGN_ALL -> {
                        // set assignee (could be null)
                        present.setAssigneeId(assignTo);
                        toUpdate.add(present);
                    }
                    case ASSIGN_UNASSIGNED -> {
                        if (!hasAssignee) {
                            present.setAssigneeId(assignTo);
                            toUpdate.add(present);
                        }
                    }
                    case CANCEL -> {
                        // nothing to do, just skip
                    }
                }
            }
        }

        // persist batch inserts and updates
        if (!toCreate.isEmpty()) {
            List<TestRunCase> saved = testRunCaseRepository.saveAll(toCreate);
            saved.forEach(s -> createdIds.add(s.getId()));
        }

        if (!toUpdate.isEmpty()) {
            testRunCaseRepository.saveAll(toUpdate);
        }

        int createdCount = toCreate.size();
        int updatedCount = toUpdate.size();

        return new AssignmentApplyResponse(createdCount, updatedCount, createdIds);
    }

    // helper
    private List<Long> fetchCaseIdsForObject(AssignmentObjectType type, Long objectId) {
        return switch (type) {
            case STORY -> testCaseRepository.findCaseIdsByTestStoryId(objectId);
            case SCENARIO -> testCaseRepository.findCaseIdsByScenarioId(objectId);
            case CASE -> {
                // ensure test case exists
                boolean exists = testCaseRepository.existsById(objectId);
                if (!exists) yield Collections.emptyList();
                yield Collections.singletonList(objectId);
            }
        };
    }
}

