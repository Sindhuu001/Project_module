package com.example.projectmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "risk")
public class RiskCategoryProperties {

    private List<Category> categories;

    @Data
    public static class Category {
        private String name;
        private String description;
    }
}
