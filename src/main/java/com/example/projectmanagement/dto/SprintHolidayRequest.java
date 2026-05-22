package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SprintHolidayRequest {
    private List<LocalDate> dates;
}
