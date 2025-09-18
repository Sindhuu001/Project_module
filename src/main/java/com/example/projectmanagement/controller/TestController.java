package com.example.projectmanagement.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.UserDto;

@RestController
@RequestMapping("/test")
public class TestController {
    private UserClient userClient;
    public TestController(UserClient userClient) {
        this.userClient = userClient;
    }
    @GetMapping("/users")
    public List<UserDto> testUserClient() {
        return userClient.findAllById();
    }
}
