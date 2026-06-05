package com.example.projectmanagement.scheduler;

import com.example.projectmanagement.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SprintScheduler {

    private final SprintService sprintService;


    // Runs every hour (cron: "0 0 * * * *"). Override via sprint.expiry.scheduler.cron property.
    @Scheduled(cron = "${sprint.expiry.scheduler.cron:0 0 * * * *}")
    public void runHourlyChecks() {
        log.info("=== SprintScheduler FIRED — checking for expired active sprints ===");
        try {
            sprintService.processExpiredSprints();
            log.info("=== SprintScheduler DONE ===");
        } catch (Exception e) {
            log.error("=== SprintScheduler ERROR: {} ===", e.getMessage(), e);
        }
    }
}
