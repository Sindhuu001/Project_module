package com.example.projectmanagement.service;

import com.example.projectmanagement.entity.Status;

import java.util.List;
import java.util.Map;

public interface StatusService {
    Status addStatus(Long projectId, Status status);
    List<Status> getStatusesByProject(Long projectId);
    void deleteStatus(Long statusId, Long newStatusId);
    List<Status> reorderStatuses(Map<Long, Integer> statusOrder);
}
