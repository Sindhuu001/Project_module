package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.SprintBurndownResponse;
import com.example.projectmanagement.dto.SprintBurndownResponse.DailyBurn;
import com.example.projectmanagement.dto.graphs.SprintBurndownResponse.ScopeChangeEvent;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.graphs.SprintBurndownSnapshot;
import com.example.projectmanagement.entity.graphs.SprintScopeChange;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.scheduler.BurndownSnapshotScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BurndownService {

    private final SprintRepository sprintRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final SprintBurndownSnapshotRepository snapshotRepository;
    private final SprintScopeChangeRepository scopeChangeRepository;
    private final BurndownSnapshotScheduler scheduler;

    @Transactional(readOnly = true)
    public SprintBurndownResponse getBurndown(Long sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));

        LocalDate sprintStart = sprint.getStartDate().toLocalDate();
        LocalDate sprintEnd   = sprint.getEndDate().toLocalDate();
        LocalDate today       = LocalDate.now();
        int totalSprintDays   = (int) ChronoUnit.DAYS.between(sprintStart, sprintEnd) + 1;

        // Load all snapshots keyed by date
        Map<LocalDate, SprintBurndownSnapshot> snapshotMap = snapshotRepository
                .findBySprintIdOrderBySnapshotDateAsc(sprintId)
                .stream()
                .collect(Collectors.toMap(SprintBurndownSnapshot::getSnapshotDate, s -> s));

        // If no snapshot yet for today and sprint is active, generate live data
        boolean hasTodaySnapshot = snapshotMap.containsKey(today);

        // Live calculation for today (used when no snapshot exists yet)
        Integer doneSortOrder = statusRepository.findMaxSortOrderByProject(sprint.getProject().getId());
        List<Story> stories = storyRepository.findBySprintId(sprintId);
        List<Task> tasks    = taskRepository.findBySprintId(sprintId);

        int liveCurrentTotal = stories.stream()
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0).sum();
        int liveCompleted = stories.stream()
                .filter(s -> s.getStatus() != null && s.getStatus().getSortOrder() == doneSortOrder)
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0).sum();
        int liveRemaining    = liveCurrentTotal - liveCompleted;
        int liveTotalIssues  = stories.size() + tasks.size();
        int liveCompletedIssues = (int) stories.stream()
                .filter(s -> s.getStatus() != null && s.getStatus().getSortOrder() == doneSortOrder)
                .count()
                + (int) tasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getSortOrder() == doneSortOrder)
                .count();

        // Initial points from first snapshot (or live if sprint just started)
        int initialPoints = snapshotMap.values().stream()
                .min(Comparator.comparing(SprintBurndownSnapshot::getSnapshotDate))
                .map(SprintBurndownSnapshot::getInitialStoryPoints)
                .orElse(liveCurrentTotal);

        // Build daily burn list
        List<DailyBurn> dailyBurnList = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .map(date -> {
                    DailyBurn d = new DailyBurn();
                    d.setDate(date);
                    d.setSprintDayNumber((int) ChronoUnit.DAYS.between(sprintStart, date) + 1);

                    DayOfWeek dow = date.getDayOfWeek();
                    d.setIsWeekend(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

                    // Ideal line — always calculable
                    int idealRemaining = Math.round(
                            initialPoints * (1f - ((float)(d.getSprintDayNumber() - 1) / (totalSprintDays - 1)))
                    );
                    d.setIdealRemainingPoints(idealRemaining);

                    if (date.isBefore(today) && snapshotMap.containsKey(date)) {
                        // Past day — use frozen snapshot
                        SprintBurndownSnapshot snap = snapshotMap.get(date);
                        d.setRemainingStoryPoints(snap.getRemainingStoryPoints());
                        d.setCompletedStoryPoints(snap.getCompletedStoryPoints());
                        d.setVelocityPoints(snap.getVelocityPoints());
                        d.setAddedScopePoints(snap.getAddedScopePoints());
                        d.setRemovedScopePoints(snap.getRemovedScopePoints());
                        d.setRemainingIssues(snap.getRemainingIssues());
                        d.setCompletedIssues(snap.getCompletedIssues());
                        d.setVelocityIssues(snap.getVelocityIssues());
                        d.setDeviationPoints(snap.getRemainingStoryPoints() - idealRemaining);

                    } else if (date.isEqual(today)) {
                        // Today — use snapshot if exists, else live
                        if (hasTodaySnapshot) {
                            SprintBurndownSnapshot snap = snapshotMap.get(today);
                            d.setRemainingStoryPoints(snap.getRemainingStoryPoints());
                            d.setCompletedStoryPoints(snap.getCompletedStoryPoints());
                            d.setVelocityPoints(snap.getVelocityPoints());
                            d.setAddedScopePoints(snap.getAddedScopePoints());
                            d.setRemovedScopePoints(snap.getRemovedScopePoints());
                            d.setRemainingIssues(snap.getRemainingIssues());
                            d.setCompletedIssues(snap.getCompletedIssues());
                            d.setVelocityIssues(snap.getVelocityIssues());
                        } else {
                            // Live fallback
                            d.setRemainingStoryPoints(liveRemaining);
                            d.setCompletedStoryPoints(liveCompleted);
                            d.setVelocityPoints(0);
                            d.setAddedScopePoints(0);
                            d.setRemovedScopePoints(0);
                            d.setRemainingIssues(liveTotalIssues - liveCompletedIssues);
                            d.setCompletedIssues(liveCompletedIssues);
                            d.setVelocityIssues(0);
                        }
                        d.setDeviationPoints(d.getRemainingStoryPoints() - idealRemaining);

                    } else {
                        // Future — nulls so frontend knows not to plot
                        d.setRemainingStoryPoints(null);
                        d.setCompletedStoryPoints(null);
                        d.setVelocityPoints(null);
                        d.setAddedScopePoints(null);
                        d.setRemovedScopePoints(null);
                        d.setRemainingIssues(null);
                        d.setCompletedIssues(null);
                        d.setVelocityIssues(null);
                        d.setDeviationPoints(null);
                    }

                    return d;
                })
                .collect(Collectors.toList());

        // Scope change events for chart markers
        List<com.example.projectmanagement.dto.graphs.SprintBurndownResponse.ScopeChangeEvent> scopeChanges = scopeChangeRepository
                .findBySprintIdOrderByChangedAtAsc(sprintId)
                .stream()
                .map(sc -> {
                    ScopeChangeEvent e = new ScopeChangeEvent();
                    e.setDate(sc.getChangedAt().toLocalDate());
                    e.setSprintDayNumber(sc.getSprintDayNumber());
                    e.setIssueTitle(sc.getIssueTitle());
                    e.setChangeType(sc.getChangeType().name());
                    e.setPointsDelta(sc.getPointsDelta());
                    e.setChangedBy(sc.getChangedBy());
                    return e;
                })
                .collect(Collectors.toList());

        // Build response
        SprintBurndownResponse response = new SprintBurndownResponse();
        response.setSprintId(sprintId);
        response.setSprintName(sprint.getName());
        response.setStartDate(sprintStart);
        response.setEndDate(sprintEnd);
        response.setTotalSprintDays(totalSprintDays);
        response.setInitialStoryPoints(initialPoints);
        response.setCurrentStoryPoints(liveCurrentTotal);
        response.setCompletedStoryPoints(liveCompleted);
        response.setRemainingStoryPoints(liveRemaining);
        response.setTotalIssues(liveTotalIssues);
        response.setCompletedIssues(liveCompletedIssues);
        response.setRemainingIssues(liveTotalIssues - liveCompletedIssues);
        response.setDailyBurn(dailyBurnList);
        response.setScopeChanges(scopeChanges);

        return response;
    }

    // Called from StoryService/TaskService when scope changes happen
    @Transactional
    public void recordScopeChange(Sprint sprint, Long issueId, String issueTitle,
                                   SprintScopeChange.IssueType issueType,
                                   SprintScopeChange.ChangeType changeType,
                                   Integer oldPoints, Integer newPoints, Long changedBy) {

        LocalDate today = LocalDate.now();
        LocalDate sprintStart = sprint.getStartDate().toLocalDate();
        int sprintDayNumber = (int) ChronoUnit.DAYS.between(sprintStart, today) + 1;

        int delta = 0;
        if (changeType == SprintScopeChange.ChangeType.ADDED_TO_SPRINT) {
            delta = newPoints != null ? newPoints : 0;
        } else if (changeType == SprintScopeChange.ChangeType.REMOVED_FROM_SPRINT) {
            delta = -(oldPoints != null ? oldPoints : 0);
        } else if (changeType == SprintScopeChange.ChangeType.STORY_POINTS_CHANGED) {
            delta = (newPoints != null ? newPoints : 0) - (oldPoints != null ? oldPoints : 0);
        }
        // STATUS_CHANGED_TO_DONE and STATUS_REOPENED have delta = 0

        SprintScopeChange change = new SprintScopeChange();
        change.setSprint(sprint);
        change.setSprintDayNumber(sprintDayNumber);
        change.setIssueId(issueId);
        change.setIssueTitle(issueTitle);
        change.setIssueType(issueType);
        change.setChangeType(changeType);
        change.setOldStoryPoints(oldPoints);
        change.setNewStoryPoints(newPoints);
        change.setPointsDelta(delta);
        change.setChangedBy(changedBy);

        scopeChangeRepository.save(change);
    }
}