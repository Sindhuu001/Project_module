package com.example.projectmanagement.ExternalDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UmsEmployeeLookupRequest {

    @JsonProperty("employee_ids")
    private List<Long> employeeIds;
}
