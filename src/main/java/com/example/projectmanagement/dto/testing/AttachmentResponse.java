package com.example.projectmanagement.dto.testing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentResponse {
    // getters/setters omitted for brevity
    private Long id;
    private String fileName;
    private String url;
    private Long runCaseId;
    private Long runCaseStepId;
    private Long uploadedBy;
    private String createdAt;

    public AttachmentResponse() {}
    public AttachmentResponse(Long id, String fileName, String url, Long runCaseId, Long runCaseStepId, Long uploadedBy, String createdAt) {
        this.id = id; this.fileName = fileName; this.url = url; this.runCaseId = runCaseId; this.runCaseStepId = runCaseStepId; this.uploadedBy = uploadedBy; this.createdAt = createdAt;
    }
}

