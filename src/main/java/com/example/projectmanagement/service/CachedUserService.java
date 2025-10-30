package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;
import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.client.UserClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedUserService {

    private final UserClient userClient;

    public CachedUserService(UserClient userClient) {
        this.userClient = userClient;
    }

    //  Cache user info
    @Cacheable(value = "users", key = "#id")
    public ExternalUserResponse getUserById(Long id) {
        System.out.println("Fetching user from user-service via Feign...");
        // simulate delay
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userClient.findExternalById(id);
    }

    //  Cache roles for each user
    @Cacheable(value = "userRoles", key = "#id")
    public ExternalRolesResponse getUserRolesById(Long id) {
        System.out.println("Fetching roles from user-service via Feign...");
        return userClient.findRolesById(id);
    }

    //  Optional — clear cache when user/roles are updated
    @CacheEvict(value = {"users", "userRoles"}, key = "#id")
    public void evictUserCache(Long id) {
        System.out.println("Cache cleared for user ID: " + id);
    }
}

