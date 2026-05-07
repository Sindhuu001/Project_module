package com.example.projectmanagement.client;

import com.example.projectmanagement.ExternalDTO.RmsResourceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.projectmanagement.config.FeignConfig;

@FeignClient(
    name = "rms-service",
    url = "${rms.service.url}",
    configuration = FeignConfig.class
)
public interface RmsClient {

    @GetMapping("/api/allocation/get-all-resources/{projectId}")
    RmsResourceResponse getProjectResources(@PathVariable("projectId") Long projectId);

}