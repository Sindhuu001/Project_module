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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

        Sprint sprint = sprintRepository.findByIdWithSchedule(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));

        LocalDate sprintStart = (sprint.getStartedAt() != null ? sprint.getStartedAt() : sprint.getStartDate()).toLocalDate();
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
                .filter(s -> s.getStatus() != null && Objects.equals(s.getStatus().getSortOrder(), doneSortOrder))
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0).sum();
        int liveRemaining    = liveCurrentTotal - liveCompleted;
        int liveTotalIssues  = stories.size() + tasks.size();
        int liveCompletedIssues = (int) stories.stream()
                .filter(s -> s.getStatus() != null && Objects.equals(s.getStatus().getSortOrder(), doneSortOrder))
                .count()
                + (int) tasks.stream()
                .filter(t -> t.getStatus() != null && Objects.equals(t.getStatus().getSortOrder(), doneSortOrder))
                .count();

        // Initial points from first snapshot (or live if sprint just started)
        int initialPoints = snapshotMap.values().stream()
                .min(Comparator.comparing(SprintBurndownSnapshot::getSnapshotDate))
                .map(SprintBurndownSnapshot::getInitialStoryPoints)
                .orElse(liveCurrentTotal);

        java.util.Set<LocalDate> holidays        = sprint.getHolidays();
        java.util.Set<LocalDate> workingWeekends = sprint.getWorkingWeekends();

        List<LocalDate> allSprintDates = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .collect(Collectors.toList());
        long totalWorkingDays = allSprintDates.stream()
                .filter(d -> isWorkingDay(d, holidays, workingWeekends))
                .count();

        // Build daily burn list
        List<DailyBurn> dailyBurnList = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .map(date -> {
                    DailyBurn d = new DailyBurn();
                    d.setDate(date);
                    d.setSprintDayNumber((int) ChronoUnit.DAYS.between(sprintStart, date) + 1);

                    DayOfWeek dow = date.getDayOfWeek();
                    d.setIsWeekend(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

                    // Ideal line — holds steady on weekends/holidays, decreases only on working days
                    long workingDaysElapsed = allSprintDates.stream()
                            .filter(wd -> !wd.isAfter(date))
                            .filter(wd -> isWorkingDay(wd, holidays, workingWeekends))
                            .count();
                    int idealRemaining = totalWorkingDays == 0 ? 0 : Math.max(0, Math.round(
                            initialPoints * (1f - ((float) workingDaysElapsed / totalWorkingDays))
                    ));
                    d.setIdealRemainingPoints(idealRemaining);

                    if (date.isBefore(today) && snapshotMap.containsKey(date)) {
                        // Past day — use frozen snapshot
                        SprintBurndownSnapshot snap = snapshotMap.get(date);
                        d.setRemainingStoryPoints(snap.getRemainingStoryPoints());
                        d.setCompletedStoryPoints(snap.getCompletedStoryPoints());
                        
                        d.setTotalScopePoints(snap.getCurrentStoryPoints());
                        d.setVelocityPoints(snap.getVelocityPoints());
                        d.setAddedScopePoints(snap.getAddedScopePoints());
                        d.setRemovedScopePoints(snap.getRemovedScopePoints());
                        d.setRemainingIssues(snap.getRemainingIssues());
                        d.setCompletedIssues(snap.getCompletedIssues());
                        d.setVelocityIssues(snap.getVelocityIssues());
                        d.setDeviationPoints(snap.getRemainingStoryPoints() - idealRemaining);
                        d.setIsHoliday(snap.getIsHoliday());
                        d.setIsWorkingWeekend(snap.getIsWorkingWeekend());

                    } else if (date.isEqual(today)) {
                        // Today — use snapshot if exists, else live
                        if (hasTodaySnapshot) {
                            SprintBurndownSnapshot snap = snapshotMap.get(today);
                            d.setRemainingStoryPoints(snap.getRemainingStoryPoints());
                            d.setCompletedStoryPoints(snap.getCompletedStoryPoints());
                            d.setTotalScopePoints(snap.getCurrentStoryPoints());
                            d.setVelocityPoints(snap.getVelocityPoints());
                            d.setAddedScopePoints(snap.getAddedScopePoints());
                            d.setRemovedScopePoints(snap.getRemovedScopePoints());
                            d.setRemainingIssues(snap.getRemainingIssues());
                            d.setCompletedIssues(snap.getCompletedIssues());
                            d.setVelocityIssues(snap.getVelocityIssues());
                            d.setIsHoliday(snap.getIsHoliday());
                            d.setIsWorkingWeekend(snap.getIsWorkingWeekend());
                        } else {
                            // Live fallback
                            d.setRemainingStoryPoints(liveRemaining);
                            d.setCompletedStoryPoints(liveCompleted);
                            d.setTotalScopePoints(liveCurrentTotal);
                            d.setVelocityPoints(0);
                            d.setAddedScopePoints(0);
                            d.setRemovedScopePoints(0);
                            d.setRemainingIssues(liveTotalIssues - liveCompletedIssues);
                            d.setCompletedIssues(liveCompletedIssues);
                            d.setVelocityIssues(0);
                            d.setIsHoliday(holidays.contains(date));
                            d.setIsWorkingWeekend(workingWeekends.contains(date));
                        }
                        d.setDeviationPoints(d.getRemainingStoryPoints() - idealRemaining);

                    } else {
                        // Future — nulls so frontend knows not to plot
                        d.setRemainingStoryPoints(null);
                        d.setCompletedStoryPoints(null);
                        d.setTotalScopePoints(null);
                        d.setVelocityPoints(null);
                        d.setAddedScopePoints(null);
                        d.setRemovedScopePoints(null);
                        d.setRemainingIssues(null);
                        d.setCompletedIssues(null);
                        d.setVelocityIssues(null);
                        d.setDeviationPoints(null);
                        d.setIsHoliday(null);
                        d.setIsWorkingWeekend(null);
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
                    e.setEpicId(sc.getEpicId());
                    e.setEpicName(sc.getEpicName());
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

    private boolean isWorkingDay(LocalDate d, Set<LocalDate> holidays, Set<LocalDate> workingWeekends) {
        if (workingWeekends.contains(d)) return true;
        DayOfWeek dw = d.getDayOfWeek();
        if (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY) return false;
        if (holidays.contains(d)) return false;
        return true;
    }

    // Called from StoryService/TaskService when scope changes happen
    @Transactional
    public void recordScopeChange(Sprint sprint, Long issueId, String issueTitle,
                                   SprintScopeChange.IssueType issueType,
                                   SprintScopeChange.ChangeType changeType,
                                   Integer oldPoints, Integer newPoints, Long changedBy, Long epicId, String epicName) {

        LocalDate today = LocalDate.now();
        LocalDate sprintStart = (sprint.getStartedAt() != null ? sprint.getStartedAt() : sprint.getStartDate()).toLocalDate();
        int sprintDayNumber = Math.max(1, (int) ChronoUnit.DAYS.between(sprintStart, today) + 1);

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
        change.setEpicId(epicId);
        change.setEpicName(epicName);

        scopeChangeRepository.save(change);

        // Trigger a real-time snapshot update after this transaction commits so the
        // burndown chart reflects the change immediately (no need to wait for midnight).
        final Long sprintId = sprint.getId();
        final LocalDate today1 = LocalDate.now();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Sprint stub = new Sprint();
                    stub.setId(sprintId);
                    scheduler.takeSnapshotForSprint(stub, today1);
                } catch (Exception e) {
                    log.error("Failed to update snapshot after scope change for sprint {}: {}", sprintId, e.getMessage());
                }
            }
        });
    }
}