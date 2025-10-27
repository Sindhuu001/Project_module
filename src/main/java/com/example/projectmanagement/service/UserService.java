package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;
import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.ExternalDTO.UserMapper;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.UserDto;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                if (user.getId() != null && user.getId().equals(id)) {
                    users.add(user);
                    break;
                }
            }
        }
        return users;
    }

    public UserDto getUserById(Long id) {
        ExternalUserResponse extUser = userClient.findExternalById(id);
        return UserMapper.toUserDto(extUser, null);
    }

    // searchUsers
    public Page<UserDto> searchUsers(String name, String role, Pageable pageable) {
        // TODO: Implement actual search logic using userClient
        List<UserDto> allUsers = userClient.findAll();
        List<UserDto> filtered = new ArrayList<>();
        for (UserDto user : allUsers) {
            boolean matches = true;
            if (name != null && !user.getName().toLowerCase().contains(name.toLowerCase())) {
                matches = false;
            }
            if (role != null && (user.getRoles() == null || !user.getRoles().contains(role))) {
                matches = false;
            }
            if (matches) {
                filtered.add(user);
            }
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    // getUsersByRole
    public List<UserDto> getUsersByRole(String role) {
        List<UserDto> allUsers = userClient.findAll();
        List<UserDto> filtered = new ArrayList<>();
        for (UserDto user : allUsers) {
            if (user.getRoles() != null && user.getRoles().contains(role)) {
                filtered.add(user);
            }
        }
        return filtered;
    }
}
