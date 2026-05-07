package com.example.projectmanagement.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenStore {

    private final AtomicReference<String> latestToken = new AtomicReference<>();

    public void store(String bearerToken) {
        latestToken.set(bearerToken);
    }

    public String get() {
        return latestToken.get();
    }
}
