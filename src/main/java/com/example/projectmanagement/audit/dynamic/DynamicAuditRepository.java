package com.example.projectmanagement.audit.dynamic;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DynamicAuditRepository {

    private final JdbcTemplate jdbc;

    public DynamicAuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createTableIfNotExists(String tableName) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "entity_id VARCHAR(255), " +
                "old_data TEXT, " +
                "new_data TEXT, " +
                "operation VARCHAR(50), " +
                "timestamp DATETIME)";
        jdbc.execute(sql);
    }

    public void insertAuditRow(String tableName,
                               String entityId,
                               String oldData,
                               String newData,
                               String operation) {

        String sql = "INSERT INTO " + tableName +
                " (entity_id, old_data, new_data, operation, timestamp) VALUES (?, ?, ?, ?, NOW())";

        jdbc.update(sql, entityId, oldData, newData, operation);
    }

    public Map<String, Object> getRawRow(String tableName, Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbc.queryForMap(sql, id);
    }
}
