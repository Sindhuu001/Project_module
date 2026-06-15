package com.example.projectmanagement.scheduler;

import com.example.projectmanagement.ExternalDTO.RmsResourceDto;
import com.example.projectmanagement.ExternalDTO.RmsResourceResponse;
import com.example.projectmanagement.client.RmsClient;
import com.example.projectmanagement.config.RmsCacheStore;
import com.example.projectmanagement.config.TokenStore;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RmsPollingService {

    private final RmsClient rmsClient;
    private final ProjectRepository projectRepository;
    private final RmsCacheStore rmsCacheStore;
    private final TokenStore tokenStore;

    // NO @Transactional on this method intentionally.
    //
    // With @Transactional here, a single Hibernate session stays open for the
    // entire poll loop. The JPA spec requires a flush before any executeUpdate()
    // (@Modifying queries). During that flush, Hibernate processes all Project
    // entities loaded by findByStatus. If the memberIds @ElementCollection is
    // initialized in that session, Hibernate can overwrite the native
    // insertMember/deleteMember changes when the outer transaction commits.
    //
    // Without @Transactional, each repository call gets its own short transaction
    // that commits immediately — no shared session, no flush-at-commit risk.
    @Scheduled(fixedRateString = "${rms.polling.interval-ms:10000}")
    public void pollRmsResources() {
        String token = tokenStore.get();
        if (token == null) {
            log.info("[RmsPolling] Skipped — no user token available yet");
            return;
        }

        List<Project> activeProjects = projectRepository.findByStatus(Project.ProjectStatus.ACTIVE);
        log.info("[RmsPolling] Started for {} active project(s)", activeProjects.size());

        for (Project proj : activeProjects) {

            // Fetch member IDs directly from RMS. employeeId == userId, no translation needed.
            List<Long> rmsIds;
            try {
                RmsResourceResponse response = rmsClient.getProjectResources(proj.getId());
                List<RmsResourceDto> resources = (response != null && response.getData() != null)
                        ? response.getData()
                        : Collections.emptyList();
                rmsCacheStore.put(proj.getId(), resources);

                rmsIds = resources.stream()
                        .map(RmsResourceDto::getResourceId)
                        .filter(id -> id != null)          // guard against null resourceId in RMS payload
                        .collect(Collectors.toList());

                log.info("[RmsPolling] Project {} → RMS returned {} member ID(s): {}", proj.getId(), rmsIds.size(), rmsIds);
            } catch (Exception e) {
                log.warn("[RmsPolling] Project {} → RMS call failed: {} — retaining existing members", proj.getId(), e.getMessage());
                continue;
            }

            if (rmsIds.isEmpty()) {
                // RMS returned empty — treat as transient/unreliable response.
                // Never wipe members based on an empty list; skip this cycle.
                log.warn("[RmsPolling] Project {} → RMS returned 0 IDs — skipping to protect existing members", proj.getId());
                continue;
            }

            Set<Long> incoming = new HashSet<>(rmsIds);

            // Read current members via a plain native SELECT — each call runs in
            // its own transaction, so this always reflects the committed DB state.
            Set<Long> current = new HashSet<>(
                    projectRepository.findMemberIdsByProjectId(proj.getId())
                            .stream()
                            .map(id -> Long.valueOf(id.toString()))  // explicit cast: guards against Integer vs Long from JDBC
                            .collect(Collectors.toSet())
            );

            log.info("[RmsPolling] Project {} → current DB members: {}, incoming RMS members: {}", proj.getId(), current, incoming);

            Set<Long> toAdd    = new HashSet<>(incoming); toAdd.removeAll(current);
            Set<Long> toRemove = new HashSet<>(current);  toRemove.removeAll(incoming);

            for (Long id : toAdd)    projectRepository.insertMember(proj.getId(), id);
            for (Long id : toRemove) projectRepository.deleteMember(proj.getId(), id);

            if (toAdd.isEmpty() && toRemove.isEmpty()) {
                log.info("[RmsPolling] Project {} → no changes ({} member(s) unchanged)", proj.getId(), current.size());
            } else {
                log.info("[RmsPolling] Project {} → synced: +{} added {}, -{} removed {}",
                        proj.getId(), toAdd.size(), toAdd, toRemove.size(), toRemove);
            }
        }

        log.info("[RmsPolling] Completed");
    }
}
