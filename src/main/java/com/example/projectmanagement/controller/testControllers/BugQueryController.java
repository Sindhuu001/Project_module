package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.testing.BugFilterRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.repository.BugRepository;
import com.example.projectmanagement.service.BugService;
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
    private final BugService bugService;

    @GetMapping
    public ResponseEntity<Page<BugResponse>> list(BugFilterRequest req) {
        int page = req.getPage();
        int size = req.getSize();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Bug> spec = BugSpecification.filter(req);
        Page<Bug> pageRes = bugRepository.findAll(spec, pageable);
        Page<BugResponse> resp = pageRes.map(bugService::toResponse);
        return ResponseEntity.ok(resp);
    }
}
