package com.example.projectmanagement.dto.testing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestScenarioData {
    private Long id;
    private String title;
    private String description;
    private List<TestCaseData> testCases;
}
