package com.example.projectmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.projectmanagement.client")
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy
public class ProjectmanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectmanagementApplication.class, args);
    }
}
