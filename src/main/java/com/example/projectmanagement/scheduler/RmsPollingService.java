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
import org.springframework.transaction.annotation.Transactional;

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

    @Scheduled(fixedRateString = "${rms.polling.interval-ms:10000}")
    @Transactional
    public void pollRmsResources() {
        String token = tokenStore.get();
        if (token == null) {
            log.info("RMS polling skipped - no user token available yet");
            return;
        }

        List<Project> activeProjects = projectRepository.findByStatus(Project.ProjectStatus.ACTIVE);
        log.info("RMS polling started for {} active projects", activeProjects.size());

        for (Project proj : activeProjects) {
            try {
                RmsResourceResponse response = rmsClient.getProjectResources(proj.getId());

                List<RmsResourceDto> resources = (response != null && response.getData() != null)
                        ? response.getData()
                        : Collections.emptyList();

                // Update in-memory cache
                rmsCacheStore.put(proj.getId(), resources);

                // Extract resource IDs and persist into project.memberIds in DB
                Set<Long> memberIds = resources.stream()
                        .map(RmsResourceDto::getResourceId)
                        .collect(Collectors.toCollection(HashSet::new));

                proj.setMemberIds(memberIds);
                projectRepository.save(proj);

                log.info("Project {} memberIds synced with {} members from RMS", proj.getId(), memberIds.size());

            } catch (Exception e) {
                log.warn("RMS polling failed for project {}: {}", proj.getId(), e.getMessage());
            

            }
        }

        log.info("RMS polling completed");
    }
}
