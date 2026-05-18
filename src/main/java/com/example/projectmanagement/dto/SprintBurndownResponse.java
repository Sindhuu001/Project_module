package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

import com.example.projectmanagement.dto.graphs.SprintBurndownResponse.ScopeChangeEvent;

@Data
public class SprintBurndownResponse {
    private Long sprintId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalStoryPoints;

    private List<DailyBurn> dailyBurn;

    @Data
    public static class DailyBurn {
        private LocalDate date;
        private Integer remaining;
        public void setSprintDayNumber(int i) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setSprintDayNumber'");
        }
        public void setIsWeekend(boolean b) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIsWeekend'");
        }
        public int getSprintDayNumber() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getSprintDayNumber'");
        }
        public void setIdealRemainingPoints(int idealRemaining) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIdealRemainingPoints'");
        }
        public void setDeviationPoints(int i) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setDeviationPoints'");
        }
        public void setVelocityIssues(Integer velocityIssues) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setVelocityIssues'");
        }
        public void setCompletedIssues(Integer completedIssues) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setCompletedIssues'");
        }
        public void setRemainingIssues(Integer remainingIssues) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setRemainingIssues'");
        }
        public void setRemovedScopePoints(Integer removedScopePoints) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setRemovedScopePoints'");
        }
        public void setAddedScopePoints(Integer addedScopePoints) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setAddedScopePoints'");
        }
        public void setVelocityPoints(Integer velocityPoints) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setVelocityPoints'");
        }
        public void setCompletedStoryPoints(Integer completedStoryPoints) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setCompletedStoryPoints'");
        }
        public void setRemainingStoryPoints(Integer remainingStoryPoints) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setRemainingStoryPoints'");
        }
        public int getRemainingStoryPoints() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRemainingStoryPoints'");
        }
        public void setDeviationPoints(Object object) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setDeviationPoints'");
        }
    }

    public void setSprintName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSprintName'");
    }

    public void setTotalSprintDays(int totalSprintDays) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTotalSprintDays'");
    }

    public void setInitialStoryPoints(int initialPoints) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setInitialStoryPoints'");
    }

    public void setCurrentStoryPoints(int liveCurrentTotal) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCurrentStoryPoints'");
    }

    public void setCompletedStoryPoints(int liveCompleted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCompletedStoryPoints'");
    }

    public void setRemainingStoryPoints(int liveRemaining) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRemainingStoryPoints'");
    }

    public void setTotalIssues(int liveTotalIssues) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTotalIssues'");
    }

    public void setCompletedIssues(int liveCompletedIssues) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCompletedIssues'");
    }

    public void setRemainingIssues(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRemainingIssues'");
    }

    public void setScopeChanges(List<ScopeChangeEvent> scopeChanges) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setScopeChanges'");
    }
}
