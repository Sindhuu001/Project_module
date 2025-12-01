package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.testing.BugFilterRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.repository.BugRepository;
import com.example.projectmanagement.service.impl.BugSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/testing/bugs")
@RequiredArgsConstructor
public class BugQueryController {

    private final BugRepository bugRepository;

    @GetMapping
    public ResponseEntity<Page<BugResponse>> list(BugFilterRequest req) {
        int page = req.getPage();
        int size = req.getSize();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Bug> spec = BugSpecification.filter(req);
        Page<Bug> pageRes = bugRepository.findAll(spec, pageable);
        Page<BugResponse> resp = pageRes.map(this::toDto);
        return ResponseEntity.ok(resp);
    }

    private BugResponse toDto(Bug b) {
        return new BugResponse(
                b.getId(),
                b.getTitle(),
                b.getStatus() != null ? b.getStatus() : null,
                b.getSeverity() != null ? b.getSeverity() : null,
                b.getPriority() != null ? b.getPriority() : null,
                b.getReporter(),
                b.getAssignedTo(),
                b.getTestRun() != null ? b.getTestRun().getId() : null,
                b.getRunCase() != null ? b.getRunCase().getId() : null,
                b.getRunCaseStep() != null ? b.getRunCaseStep().getId() : null,
                b.getTestCase() != null ? b.getTestCase().getId() : null,
                b.getTestScenario() != null ? b.getTestScenario().getId() : null,
                b.getTestStory() != null ? b.getTestStory().getId() : null,
                b.getProject() != null ? b.getProject().getId() : null,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}

