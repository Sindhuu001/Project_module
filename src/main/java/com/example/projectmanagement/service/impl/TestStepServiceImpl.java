package com.example.projectmanagement.service.impl;
import com.example.projectmanagement.dto.testing.TestStepCreateRequest;
import com.example.projectmanagement.dto.testing.TestStepResponse;
import com.example.projectmanagement.dto.testing.TestStepsReorderRequest;
import com.example.projectmanagement.entity.testing.TestCase;
import com.example.projectmanagement.entity.testing.TestStep;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestStepRepository;
import com.example.projectmanagement.service.TestStepService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestStepServiceImpl implements TestStepService {

    private final TestStepRepository testStepRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    public List<TestStepResponse> getStepsForCase(Long caseId) {
        List<TestStep> steps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(caseId);
        return toDtos(steps);
    }

    @Override
    @Transactional
    public List<TestStepResponse> addStepsToCase(Long caseId, List<TestStepCreateRequest> steps) {
        if (steps == null || steps.isEmpty()) {
            return getStepsForCase(caseId);
        }

        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Test Case not found: " + caseId));

        List<TestStep> existing = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(caseId);
        int startNumber = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getStepNumber() + 1;

        List<TestStep> newSteps = new ArrayList<>(steps.size());
        int stepNumber = startNumber;
        for (TestStepCreateRequest req : steps) {
            TestStep step = TestStep.builder()
                    .testCase(testCase)
                    .stepNumber(stepNumber++)
                    .action(req.action())
                    .expectedResult(req.expectedResult())
                    .build();
            newSteps.add(step);
        }

        testStepRepository.saveAll(newSteps);

        List<TestStep> allSteps = new ArrayList<>(existing.size() + newSteps.size());
        allSteps.addAll(existing);
        allSteps.addAll(newSteps);

        return toDtos(allSteps);
    }

    @Override
    @Transactional
    public TestStepResponse updateStep(Long stepId, TestStepCreateRequest request) {
        TestStep step = testStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Test Step not found: " + stepId));

        step.setAction(request.action());
        step.setExpectedResult(request.expectedResult());

        TestStep saved = testStepRepository.save(step);

        return new TestStepResponse(
                saved.getId(),
                saved.getStepNumber(),
                saved.getAction(),
                saved.getExpectedResult()
        );
    }

    @Override
    @Transactional
    public List<TestStepResponse> reorderSteps(Long caseId, TestStepsReorderRequest request) {
        List<TestStep> steps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(caseId);
        Map<Long, TestStep> stepMap = new HashMap<>();
        for (TestStep step : steps) {
            stepMap.put(step.getId(), step);
        }

        int stepNumber = 1;
        List<TestStep> reordered = new ArrayList<>(steps.size());
        for (Long stepId : request.stepIdsInOrder()) {
            TestStep step = stepMap.get(stepId);
            if (step == null) {
                throw new IllegalArgumentException("Step ID " + stepId + " does not belong to case " + caseId);
            }
            step.setStepNumber(stepNumber++);
            reordered.add(step);
        }

        testStepRepository.saveAll(reordered);

        reordered.sort(Comparator.comparingInt(TestStep::getStepNumber));
        return toDtos(reordered);
    }

    private List<TestStepResponse> toDtos(List<TestStep> steps) {
        return steps.stream()
                .map(s -> new TestStepResponse(
                        s.getId(),
                        s.getStepNumber(),
                        s.getAction(),
                        s.getExpectedResult()
                ))
                .toList();
    }
}
