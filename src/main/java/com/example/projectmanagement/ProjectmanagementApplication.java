package com.example.projectmanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.projectmanagement.client")
public class ProjectmanagementApplication {

    public static void main(String[] args) {

        // ✅ Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // ✅ Set them as system properties (Spring Boot will pick them up)
        System.setProperty("SPRING_APPLICATION_NAME", dotenv.get("SPRING_APPLICATION_NAME"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_DRIVER", dotenv.get("DB_DRIVER"));
        System.setProperty("JPA_DDL", dotenv.get("JPA_DDL"));
        System.setProperty("JPA_SHOW_SQL", dotenv.get("JPA_SHOW_SQL"));
        System.setProperty("JPA_DIALECT", dotenv.get("JPA_DIALECT"));
        System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));

        // ✅ Now run Spring Boot
        SpringApplication.run(ProjectmanagementApplication.class, args);
    }
}
