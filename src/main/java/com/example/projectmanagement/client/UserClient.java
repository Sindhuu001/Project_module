package com.example.projectmanagement.client;

import com.example.projectmanagement.ExternalDTO.ExternalUserResponse;
import com.example.projectmanagement.ExternalDTO.UmsEmployeeInfo;
import com.example.projectmanagement.ExternalDTO.UmsEmployeeLookupRequest;
import com.example.projectmanagement.dto.UserDto;


import com.example.projectmanagement.ExternalDTO.ExternalRolesResponse;

import java.util.List;

// import org.hibernate.mapping.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
@FeignClient(
    name = "user-service",
    url = "${user.service.url}",
    // url = "http://192.168.2.69:8000",
    configuration = com.example.projectmanagement.config.FeignConfig.class
    
)
public interface UserClient {
    // System.out.print(configuration);

    @GetMapping("admin/users/{id}")
    ExternalUserResponse findExternalById(@PathVariable("id") Long id);

    @GetMapping("admin/users/{id}/roles")
    ExternalRolesResponse findRolesById(@PathVariable("id") Long id);

     @GetMapping("admin/users/id/roles")
    List<UserDto> findAll();
    // Inside UserClient.java
@GetMapping("/admin/roles/users/role_name")
List<Map<String, Object>> getUsersByExternalRole(@RequestParam("role_name") String roleName);

    @PostMapping("/admin/users/employee/ids")
    Map<String, UmsEmployeeInfo> resolveEmployeeIds(@RequestBody UmsEmployeeLookupRequest request);

}
