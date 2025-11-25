package com.example.projectmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "risk")
@Data
public class RiskConfig {

    private List<Category> categories;
    private List<String> defaultStatuses;

    @Data
    public static class Category {
        private String name;
        private String description;
    }
}
