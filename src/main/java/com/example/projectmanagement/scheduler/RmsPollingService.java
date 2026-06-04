package com.example.projectmanagement.scheduler;

import com.example.projectmanagement.ExternalDTO.RmsResourceDto;
import com.example.projectmanagement.ExternalDTO.RmsResourceResponse;
import com.example.projectmanagement.ExternalDTO.UmsEmployeeInfo;
import com.example.projectmanagement.ExternalDTO.UmsEmployeeLookupRequest;
import com.example.projectmanagement.client.RmsClient;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.config.RmsCacheStore;
import com.example.projectmanagement.config.TokenStore;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



@Slf4j
@Service
@RequiredArgsConstructor
public class RmsPollingService {

    private final RmsClient rmsClient;
    private final UserClient userClient;
    private final ProjectRepository projectRepository;
    private final RmsCacheStore rmsCacheStore;
    private final TokenStore tokenStore;

    @Scheduled(fixedRateString = "${rms.polling.interval-ms:10000}")
    @org.springframework.transaction.annotation.Transactional
    public void pollRmsResources() {
        String token = tokenStore.get();
        if (token == null) {
            log.info("RMS polling skipped - no user token available yet");
            return;
        }

        List<Project> activeProjects = projectRepository.findByStatus(Project.ProjectStatus.ACTIVE);
        log.info("RMS polling started for {} active projects", activeProjects.size());

        for (Project proj : activeProjects) {

            // Step 1: fetch employee IDs from RMS
            List<Long> employeeIds;
            try {
                RmsResourceResponse response = rmsClient.getProjectResources(proj.getId());
                List<RmsResourceDto> resources = (response != null && response.getData() != null)
                        ? response.getData()
                        : Collections.emptyList();
                rmsCacheStore.put(proj.getId(), resources);
                employeeIds = resources.stream()
                        .map(RmsResourceDto::getResourceId)
                        .collect(Collectors.toList());
                log.info("Project {} → RMS returned {} employee IDs: {}", proj.getId(), employeeIds.size(), employeeIds);
            } catch (Exception e) {
                log.warn("Project {} → RMS call failed: {}", proj.getId(), e.getMessage());
                continue;
            }

            if (employeeIds.isEmpty()) {
                projectRepository.deleteAllMembers(proj.getId());
                log.info("Project {} → no RMS members, cleared project_members", proj.getId());
                continue;
            }

            // Step 2: resolve employee IDs → UMS user IDs
            try {
                log.info("Project {} → calling UMS with employee IDs: {}", proj.getId(), employeeIds);
                Map<String, UmsEmployeeInfo> umsLookup = userClient.resolveEmployeeIds(
                        new UmsEmployeeLookupRequest(employeeIds));
                log.info("Project {} → UMS raw response: {}", proj.getId(), umsLookup);

                Set<Long> umsUserIds = umsLookup.values().stream()
                        .filter(info -> info != null && info.getUserId() != null)
                        .map(UmsEmployeeInfo::getUserId)
                        .collect(Collectors.toCollection(HashSet::new));

                // DELETE all existing rows then INSERT resolved UMS IDs — avoids any
                // Hibernate lazy-load of stale employee IDs contaminating the write.
                projectRepository.deleteAllMembers(proj.getId());
                for (Long umsId : umsUserIds) {
                    projectRepository.insertMember(proj.getId(), umsId);
                }
                log.info("Project {} → saved UMS user IDs: {}", proj.getId(), umsUserIds);

            } catch (Exception e) {
                log.error("Project {} → UMS resolution failed for employee IDs {}: {} — retaining existing members",
                        proj.getId(), employeeIds, e.getMessage(), e);
            }
        }

        log.info("RMS polling completed");
    }
}
