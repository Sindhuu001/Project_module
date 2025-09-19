package com.example.projectmanagement.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class UserDto {

    // Accept "user_id" from user-service, output as "id"
    @JsonAlias("user_id")
    @JsonProperty("id")
    private Long id;

    // Explicitly map "name"
    @JsonProperty("name")
    private String name;

    // Accept "mail" from user-service, output as "email"
    @JsonAlias("mail")
    @JsonProperty("email")
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @JsonProperty("roles")
    private List<String> roles;

    public UserDto() {}
}
