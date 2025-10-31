package com.example.projectmanagement.ExternalDTO;


import com.example.projectmanagement.dto.UserDto;

import java.util.List;

public class UserMapper {

    public static UserDto toUserDto(ExternalUserResponse extUser, List<String> roles) {
        return UserDto.builder()
                .id(extUser.getUser_id())
                .name(extUser.getFirst_name() + " " + extUser.getLast_name())
                .email(extUser.getMail())
                .roles(roles)
                .build();
    }
}
