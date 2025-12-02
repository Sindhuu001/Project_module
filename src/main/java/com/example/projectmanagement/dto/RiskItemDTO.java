package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskItemDTO {
    private Long id;
    private String title;
    private int prob;
    private int impact;
    private int riskScore;
    private String severity;    // High / Medium / Low
    private String status;      // Lifecycle status (Mitigated / Monitoring / Open / Closed)
    private String owner;
    private String reporter;
    private Long ownerId;
    private Long reporterId;
}
