package com.example.projectmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "project")
public class ProjectStatusProperties {

    private List<StatusProperty> defaultStatuses = new ArrayList<>();

    public List<StatusProperty> getDefaultStatuses() {
        return defaultStatuses;
    }

    public void setDefaultStatuses(List<StatusProperty> defaultStatuses) {
        this.defaultStatuses = defaultStatuses;
    }

    public static class StatusProperty {
        private String name;
        private int sortOrder;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
