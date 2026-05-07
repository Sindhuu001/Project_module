package com.example.projectmanagement.ExternalDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RmsResourceDto {

    @JsonProperty("resourceId")
    private Long resourceId;

    @JsonProperty("resourceName")
    private String resourceName;

    @JsonProperty("resourceRole")
    private String resourceRole;
}
