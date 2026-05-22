package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.SprintBurnupResponse;
import com.example.projectmanagement.dto.SprintBurnupResponse.DailyBurnup;
import com.example.projectmanagement.dto.SprintBurnupResponse.ScopeChangeEvent;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.graphs.SprintBurndownSnapshot;
import com.example.projectmanagement.entity.graphs.SprintScopeChange;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.repository.SprintBurndownSnapshotRepository;
import com.example.projectmanagement.repository.SprintScopeChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BurnupService {

    private final SprintRepository sprintRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final SprintBurndownSnapshotRepository snapshotRepository;
    private final SprintScopeChangeRepository scopeChangeRepository;

    @Transactional(readOnly = true)
    public SprintBurnupResponse getBurnup(Long sprintId) {

        // ── Load sprint ───────────────────────────────────────────────────

        Sprint sprint = sprintRepository.findByIdWithSchedule(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));

        LocalDate sprintStart = (sprint.getStartedAt() != null ? sprint.getStartedAt() : sprint.getStartDate()).toLocalDate();
        LocalDate sprintEnd   = sprint.getEndDate().toLocalDate();
        LocalDate today       = LocalDate.now();
        int totalSprintDays   = (int) ChronoUnit.DAYS.between(sprintStart, sprintEnd) + 1;

        Set<LocalDate> holidays        = sprint.getHolidays();
        Set<LocalDate> workingWeekends = sprint.getWorkingWeekends();

        List<LocalDate> allSprintDates = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .collect(Collectors.toList());

        long totalWorkingDays = allSprintDates.stream()
                .filter(wd -> isWorkingDay(wd, holidays, workingWeekends))
                .count();

        // ── Load all snapshots keyed by date ──────────────────────────────

        Map<LocalDate, SprintBurndownSnapshot> snapshotMap = snapshotRepository
                .findBySprintIdOrderBySnapshotDateAsc(sprintId)
                .stream()
                .collect(Collectors.toMap(
                        SprintBurndownSnapshot::getSnapshotDate,
                        s -> s
                ));

        // ── Live calculations for today (fallback if no snapshot yet) ─────

        Integer doneSortOrder = statusRepository.findMaxSortOrderByProject(sprint.getProject().getId());
        List<Story> stories   = storyRepository.findBySprintId(sprintId);
        List<Task> tasks      = taskRepository.findBySprintId(sprintId);

        int liveCurrentTotal = stories.stream()
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int liveCompleted = stories.stream()
                .filter(s -> s.getStatus() != null
                        && doneSortOrder.equals(s.getStatus().getSortOrder()))
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int liveRemaining   = liveCurrentTotal - liveCompleted;
        int liveTotalIssues = stories.size() + tasks.size();

        int liveCompletedIssues = (int) stories.stream()
                .filter(s -> s.getStatus() != null
                        && doneSortOrder.equals(s.getStatus().getSortOrder()))
                .count()
                + (int) tasks.stream()
                .filter(t -> t.getStatus() != null
                        && doneSortOrder.equals(t.getStatus().getSortOrder()))
                .count();

        // ── Initial points — locked from day 1 snapshot ───────────────────

        int initialPoints = snapshotMap.values().stream()
                .min(Comparator.comparing(SprintBurndownSnapshot::getSnapshotDate))
                .map(SprintBurndownSnapshot::getInitialStoryPoints)
                .orElse(liveCurrentTotal);

        boolean hasTodaySnapshot = snapshotMap.containsKey(today);

        // ── Build per-day burnup list ─────────────────────────────────────

        List<DailyBurnup> dailyBurnupList = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .map(date -> {
                    DailyBurnup d = new DailyBurnup();
                    d.setDate(date);
                    d.setSprintDayNumber((int) ChronoUnit.DAYS.between(sprintStart, date) + 1);

                    DayOfWeek dow = date.getDayOfWeek();
                    d.setIsWeekend(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

                    // Ideal completed line: increases only on working days
                    long workingDaysElapsed = allSprintDates.stream()
                            .filter(wd -> !wd.isAfter(date))
                            .filter(wd -> isWorkingDay(wd, holidays, workingWeekends))
                            .count();
                    int idealCompleted = totalWorkingDays == 0 ? 0 : Math.min(initialPoints,
                            Math.round(initialPoints * ((float) workingDaysElapsed / totalWorkingDays)));

                    d.setIdealCompletedPoints(idealCompleted);
                    d.setInitialScopePoints(initialPoints);

                    if (date.isBefore(today) && snapshotMap.containsKey(date)) {
                        // ── Past day: use frozen snapshot ─────────────────
                        SprintBurndownSnapshot snap = snapshotMap.get(date);

                        d.setCompletedPoints(snap.getCompletedStoryPoints());
                        d.setTotalScopePoints(snap.getCurrentStoryPoints());
                        d.setVelocityPoints(snap.getVelocityPoints());
                        d.setVelocityIssues(snap.getVelocityIssues());
                        d.setAddedScopePoints(snap.getAddedScopePoints());
                        d.setRemovedScopePoints(snap.getRemovedScopePoints());
                        d.setCompletedIssues(snap.getCompletedIssues());
                        d.setTotalIssues(snap.getTotalIssues());
                        d.setRemainingIssues(snap.getRemainingIssues());
                        d.setDeviationPoints(snap.getCompletedStoryPoints() - idealCompleted);
                        d.setIsHoliday(snap.getIsHoliday());
                        d.setIsWorkingWeekend(snap.getIsWorkingWeekend());

                    } else if (date.isEqual(today)) {
                        // ── Today: snapshot if available, else live ───────
                        if (hasTodaySnapshot) {
                            SprintBurndownSnapshot snap = snapshotMap.get(today);

                            d.setCompletedPoints(snap.getCompletedStoryPoints());
                            d.setTotalScopePoints(snap.getCurrentStoryPoints());
                            d.setVelocityPoints(snap.getVelocityPoints());
                            d.setVelocityIssues(snap.getVelocityIssues());
                            d.setAddedScopePoints(snap.getAddedScopePoints());
                            d.setRemovedScopePoints(snap.getRemovedScopePoints());
                            d.setCompletedIssues(snap.getCompletedIssues());
                            d.setTotalIssues(snap.getTotalIssues());
                            d.setRemainingIssues(snap.getRemainingIssues());
                            d.setIsHoliday(snap.getIsHoliday());
                            d.setIsWorkingWeekend(snap.getIsWorkingWeekend());
                        } else {
                            // Live fallback — no snapshot yet for today
                            d.setCompletedPoints(liveCompleted);
                            d.setTotalScopePoints(liveCurrentTotal);
                            d.setVelocityPoints(0);
                            d.setVelocityIssues(0);
                            d.setAddedScopePoints(0);
                            d.setRemovedScopePoints(0);
                            d.setCompletedIssues(liveCompletedIssues);
                            d.setTotalIssues(liveTotalIssues);
                            d.setRemainingIssues(liveTotalIssues - liveCompletedIssues);
                            d.setIsHoliday(holidays.contains(date));
                            d.setIsWorkingWeekend(workingWeekends.contains(date));
                        }
                        d.setDeviationPoints(d.getCompletedPoints() - idealCompleted);

                    } else {
                        // ── Future day: nulls so frontend doesn't plot ────
                        d.setCompletedPoints(null);
                        d.setTotalScopePoints(null);
                        d.setVelocityPoints(null);
                        d.setVelocityIssues(null);
                        d.setAddedScopePoints(null);
                        d.setRemovedScopePoints(null);
                        d.setCompletedIssues(null);
                        d.setTotalIssues(null);
                        d.setRemainingIssues(null);
                        d.setDeviationPoints(null);
                        d.setIsHoliday(null);
                        d.setIsWorkingWeekend(null);
                    }

                    return d;
                })
                .collect(Collectors.toList());

        // ── Scope change events for chart markers ─────────────────────────

        List<ScopeChangeEvent> scopeChanges = scopeChangeRepository
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

        // ── Sprint health ─────────────────────────────────────────────────

        int completionPct = liveCurrentTotal > 0
                ? Math.round((liveCompleted * 100f) / liveCurrentTotal)
                : 0;

        // Guard: sprint hasn't started yet
        boolean isOnTrack;
        int overallDeviation;

        if (today.isBefore(sprintStart)) {
            isOnTrack = true;
            overallDeviation = 0;
        } else {
            LocalDate clampedToday = today.isAfter(sprintEnd) ? sprintEnd : today;
            long todayWorkingElapsed = allSprintDates.stream()
                    .filter(wd -> !wd.isAfter(clampedToday))
                    .filter(wd -> isWorkingDay(wd, holidays, workingWeekends))
                    .count();
            int todayIdeal = totalWorkingDays == 0 ? 0 : Math.min(initialPoints,
                    Math.round(initialPoints * ((float) todayWorkingElapsed / totalWorkingDays)));

            isOnTrack = liveCompleted >= todayIdeal;
            overallDeviation = liveCompleted - todayIdeal;
        }

        // ── Build response ────────────────────────────────────────────────

        SprintBurnupResponse response = new SprintBurnupResponse();
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
        response.setCompletionPercentage(completionPct);
        response.setIsOnTrack(isOnTrack);
        response.setDeviationPoints(overallDeviation);
        response.setDailyBurnup(dailyBurnupList);
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
}