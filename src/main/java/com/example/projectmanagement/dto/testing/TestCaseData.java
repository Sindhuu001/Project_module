package com.example.projectmanagement.dto.testing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseData {
    private Long id;
    private String title;
    private String preConditions;
    private String type;
    private String priority;
    private String status;
}
