package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.StatusDto;
import java.util.List;
import java.util.Map;

public interface StatusService {
    StatusDto addStatus(Long projectId, StatusDto statusDto);
    List<StatusDto> getStatusesByProject(Long projectId);
    void deleteStatus(Long statusId, Long newStatusId);
    List<StatusDto> reorderStatuses(Map<Long, Integer> statusOrder);
    List<StatusDto> syncStatuses(Long projectId, List<StatusDto> desiredStatuses);
}
