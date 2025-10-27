package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.BugDto;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.repository.BugRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BugService {

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private ModelMapper modelMapper;

    public BugDto createBug(BugDto bugDto) {
        Bug bug = modelMapper.map(bugDto, Bug.class);
        bug.setCreatedDate(LocalDateTime.now());
        bug.setUpdatedDate(LocalDateTime.now());
        Bug saved = bugRepository.save(bug);
        return modelMapper.map(saved, BugDto.class);
    }

    public List<BugDto> getAllBugs() {
        return bugRepository.findAll()
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public BugDto getBugById(Long id) {
        return bugRepository.findById(id)
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .orElseThrow(() -> new RuntimeException("Bug not found with ID: " + id));
    }

    public List<BugDto> getBugsByProject(Long projectId) {
        return bugRepository.findByProjectId(projectId)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public List<BugDto> getBugsByAssignee(Long userId) {
        return bugRepository.findByAssignedTo(userId)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public List<BugDto> getBugsBySprint(Long sprintId) {
        return bugRepository.findBySprintId(sprintId)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public List<BugDto> getBugsByStatus(Bug.Status status) {
        return bugRepository.findByStatus(status)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public List<BugDto> getBugsBySeverity(Bug.Severity severity) {
        return bugRepository.findBySeverity(severity)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public BugDto updateBug(Long id, BugDto updatedBugDto) {
        Bug existingBug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found with ID: " + id));

        modelMapper.map(updatedBugDto, existingBug);
        existingBug.setUpdatedDate(LocalDateTime.now());

        Bug saved = bugRepository.save(existingBug);
        return modelMapper.map(saved, BugDto.class);
    }

    public BugDto updateBugStatus(Long id, Bug.Status status) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found with ID: " + id));

        bug.setStatus(status);
        if (status == Bug.Status.RESOLVED || status == Bug.Status.CLOSED) {
            bug.setResolvedDate(LocalDateTime.now());
        }
        bug.setUpdatedDate(LocalDateTime.now());
        bugRepository.save(bug);
        return modelMapper.map(bug, BugDto.class);
    }

    public void deleteBug(Long id) {
        bugRepository.deleteById(id);
    }

    public List<BugDto> getBugsByEpic(Long epicId) {
        return bugRepository.findByEpicId(epicId)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

    public List<BugDto> getBugsByTask(Long taskId) {
        return bugRepository.findByTaskId(taskId)
                .stream()
                .map(bug -> modelMapper.map(bug, BugDto.class))
                .collect(Collectors.toList());
    }

}
