package com.example.projectmanagement.dto.testing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStoryData {
    private Long id;
    private String name;
    private String description;
    private List<TestScenarioData> scenarios;
}
