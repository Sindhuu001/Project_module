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


    // configured in properties by default "0 0 * * * *" (= every hour at minute 0)
    @Scheduled(cron = "${sprint.scheduler.cron:0 0 * * * *}")
    public void runHourlyChecks() {
        log.info("SprintScheduler running: processing expired sprints if any.");
        sprintService.processExpiredSprints();
        log.info("completed executing...");
    }
}
