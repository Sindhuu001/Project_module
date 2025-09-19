package com.example.projectmanagement.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.service.UserService;

@RestController
@RequestMapping("/test")
public class TestController {
    private UserClient userClient;
    private UserService userService;
    public TestController(UserClient userClient, UserService userService) {
        this.userClient = userClient;
        this.userService = userService;
    }
    @GetMapping("/users")
    public List<UserDto> testUserClient() {
        return userClient.findAll();
    }
    @GetMapping("/users/{id}")
    public UserDto testUserClientById(@PathVariable Long id) {
        return userService.getUserWithRoles(id);
    }

}
