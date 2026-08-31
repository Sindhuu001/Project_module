package com.example.projectmanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ColumnMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        resizeColumn("stories", "description",  1000);
        resizeColumn("tasks",   "description",  1000);
        resizeColumn("epic",    "description",  1000);
    }

    private void resizeColumn(String table, String column, int newLength) {
        try {
            // Read current column size from information_schema
            String sql = """
                    SELECT CHARACTER_MAXIMUM_LENGTH
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME   = ?
                      AND COLUMN_NAME  = ?
                    """;

            Integer currentLength = jdbcTemplate.queryForObject(sql, Integer.class, table, column);

            if (currentLength == null || currentLength < newLength) {
                jdbcTemplate.execute(
                        "ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` VARCHAR(" + newLength + ")");
                log.info("Resized {}.{} from {} to VARCHAR({})", table, column, currentLength, newLength);
            }
        } catch (Exception e) {
            log.warn("Could not resize {}.{}: {}", table, column, e.getMessage());
        }
    }
}
