package com.example.projectmanagement.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    private final TokenStore tokenStore;

    public FeignConfig(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String authHeader = null;

            // Try to get token from the current HTTP request (regular user calls)
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                authHeader = request.getHeader("Authorization");
            }

            // Fall back to the last captured token (for scheduled polling jobs)
            if (authHeader == null) {
                authHeader = tokenStore.get();
            }

            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        };
    }
}
