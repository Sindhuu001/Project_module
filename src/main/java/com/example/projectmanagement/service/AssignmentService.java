package com.example.projectmanagement.service;
import com.example.projectmanagement.dto.testing.AssignmentApplyRequest;
import com.example.projectmanagement.dto.testing.AssignmentApplyResponse;
import com.example.projectmanagement.dto.testing.AssignmentValidateRequest;
import com.example.projectmanagement.dto.testing.AssignmentValidateResponse;

public interface AssignmentService {

    AssignmentValidateResponse validateAssignment(AssignmentValidateRequest req);

    AssignmentApplyResponse applyAssignment(AssignmentApplyRequest req, Long currentUserId);
}
