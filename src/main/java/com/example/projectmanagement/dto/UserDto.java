package com.example.projectmanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {

    @JsonProperty("user_id")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @JsonProperty("mail")
    private String email;

    private List<String> roles;

    // Constructors
    public UserDto() {}

    public UserDto(Long id, String name, List<String> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public UserDto(String name, String email, List<String> roles) {
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    
}
