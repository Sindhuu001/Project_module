// ======================================================
// RiskPaginationDTO.java
// ======================================================

package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskPaginationDTO {

    private int page;

    private int size;

    private int totalPages;

    private long totalItems;
}