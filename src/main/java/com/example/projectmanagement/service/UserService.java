package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;
import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.ExternalDTO.UserMapper;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.UserDto;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.projectmanagement.dto.UserSummaryDto;
import com.example.projectmanagement.service.CachedUserService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient;

    
    private final CachedUserService cachedUserService;

    // ✅ Get single user with roles
    public UserDto getUserWithRoles(Long id) {
        ExternalUserResponse extUser = cachedUserService.getUserById(id);
        ExternalRolesResponse rolesResponse = cachedUserService.getUserRolesById(id);

        return UserMapper.toUserDto(extUser, rolesResponse.getRoles());
    }

    // ✅ Existing method (kept as-is but safer)
    public List<UserDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserDto> users = new ArrayList<>();
        List<UserDto> allUsers = userClient.findAll(); // ⚠️ expensive call

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

    // ✅ NEW METHOD (Fixes your compile error)
    public Map<Long, UserDto> getUsersByIdsMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserDto> users = getUsersByIds(new ArrayList<>(ids));

        return users.stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(UserDto::getId, u -> u));
    }

    // ✅ Single user without roles
    public UserDto getUserById(Long id) {
        ExternalUserResponse extUser = cachedUserService.getUserById(id);
        return UserMapper.toUserDto(extUser, null);
    }

    // ✅ Search users
    public Page<UserDto> searchUsers(String name, String role, Pageable pageable) {
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
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
    // Add this import at the top

    // Inside UserService class
    public List<UserSummaryDto> getLightweightUsersByRoleName(String roleName) {
        // 1. Get the list of user_ids from the external backend
        List<Map<String, Object>> externalResponse = userClient.getUsersByExternalRole(roleName);

        if (externalResponse == null || externalResponse.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. For each user_id, get the name using your existing cache logic
        return externalResponse.stream()
                .map(item -> {
                    Long id = Long.valueOf(item.get("user_id").toString());
                    // Using your existing getUserById which uses the cache
                    UserDto fullUser = getUserById(id);
                    String name = (fullUser != null) ? fullUser.getName() : "Unknown User";

                    return new UserSummaryDto(id, name);
                })
                .collect(Collectors.toList());
    }

    // ✅ Users by role
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
