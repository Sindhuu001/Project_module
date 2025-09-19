package com.example.projectmanagement.client;

import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    url = "http://192.168.2.100:8000",
    configuration = com.example.projectmanagement.config.FeignConfig.class
)
public interface UserClient {

    @GetMapping("admin/users/{id}")
    ExternalUserResponse findExternalById(@PathVariable("id") Long id);

    @GetMapping("admin/users/{id}/roles")
    ExternalRolesResponse findRolesById(@PathVariable("id") Long id);

     @GetMapping("admin/users/roles")
    List<UserDto> findAll();
}
