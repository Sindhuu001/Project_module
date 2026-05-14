package com.example.projectmanagement.ExternalDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UmsEmployeeInfo {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_uuid")
    private String userUuid;

    @JsonProperty("message")
    private String message;
}
