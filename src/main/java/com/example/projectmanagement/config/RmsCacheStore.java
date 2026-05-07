package com.example.projectmanagement.config;

import com.example.projectmanagement.ExternalDTO.RmsResourceDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RmsCacheStore {

    private final ConcurrentHashMap<Long, List<RmsResourceDto>> cache = new ConcurrentHashMap<>();

    public void put(Long projectId, List<RmsResourceDto> resources) {
        cache.put(projectId, resources);
    }

    public List<RmsResourceDto> get(Long projectId) {
        return cache.getOrDefault(projectId, Collections.emptyList());
    }

    public boolean contains(Long projectId) {
        return cache.containsKey(projectId);
    }
}
