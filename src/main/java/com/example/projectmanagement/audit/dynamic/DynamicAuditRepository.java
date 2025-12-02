package com.example.projectmanagement.audit.dynamic;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
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
                "operation VARCHAR(50), " +
                "user_id BIGINT, " +
                "old_data TEXT, " +
                "new_data TEXT, " +
                "ip_address VARCHAR(100), " +
                "`host` VARCHAR(255), " +        // host is a reserved keyword
                "`timestamp` DATETIME, " +
                "endpoint VARCHAR(500)" +
                ")";
        jdbc.execute(sql);
    }


    public void insertAuditRow(
        String tableName,
        String entityId,
        String operation,
        Long userId,
        String oldData,
        String newData,
        String ipAddress,
        String host,
        LocalDateTime timestamp,
        String endpoint
    ) {
        String sql = "INSERT INTO " + tableName + " " +
        "(entity_id, operation, user_id, old_data, new_data, ip_address, `host`, `timestamp`, endpoint) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


        jdbc.update(
                sql,
                entityId,
                operation,
                userId,
                oldData,
                newData,
                ipAddress,
                host,
                Timestamp.valueOf(timestamp),
                endpoint
        );
    }


    public Map<String, Object> getRawRow(String tableName, Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbc.queryForMap(sql, id);
    }

    public List<Map<String, Object>> getHistoryRows(String tableName, Long entityId) {
        String sql = "SELECT * FROM " + tableName + " WHERE entity_id = ? ORDER BY timestamp DESC LIMIT 5";
        return jdbc.queryForList(sql, entityId);
    }

}
