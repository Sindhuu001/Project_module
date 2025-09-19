package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;
import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.ExternalDTO.UserMapper;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.UserDto;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient;

    public UserDto getUserWithRoles(Long id) {
        ExternalUserResponse extUser = userClient.findExternalById(id);
        ExternalRolesResponse rolesResponse = userClient.findRolesById(id);
        return UserMapper.toUserDto(extUser, rolesResponse.getRoles());
    }

    public  List<UserDto> getUsersByIds(List<Long> ids) {
        List<UserDto> users = new ArrayList<>();
        List<UserDto> allUsers = userClient.findAll();
        for (Long id : ids) {
            for (UserDto user : allUsers) {
                if (user.getId().equals(id)) {
                    users.add(user);
                    break;
                }
            }
        }
        return users;
    }
}
