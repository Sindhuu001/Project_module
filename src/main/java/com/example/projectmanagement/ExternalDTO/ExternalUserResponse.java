package com.example.projectmanagement.ExternalDTO;

import lombok.Data;

@Data
public class ExternalUserResponse {
    private Long user_id;
    private String first_name;
    private String last_name;
    private String mail;
    private String contact;
    private String password;
    private Boolean is_active;
}
