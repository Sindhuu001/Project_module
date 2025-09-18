package com.example.projectmanagement.client;

import com.example.projectmanagement.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "user-service", 
    url = "http://192.168.2.80:8000", 
    configuration = com.example.projectmanagement.config.FeignConfig.class) // 👈                                                                                                   // URL
public interface UserClient {

    // Check if user exists
    // @GetMapping("/users/{id}/exists")
    // boolean existsById(@PathVariable("id") Long id);

    // Get user by ID
    @GetMapping("admin/users/{id}")
    UserDto findById(@PathVariable("id") Long id);

    // Get multiple users by IDs
    @GetMapping("admin/users/roles")
    List<UserDto> findAllById();

}
