package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RiskResponseDTO {
    private Long issueId;
    private RiskSummaryDTO summary;
    private PaginationDTO pagination;
    private List<RiskItemDTO> items;
}
