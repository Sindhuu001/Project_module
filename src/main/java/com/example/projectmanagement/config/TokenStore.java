package com.example.projectmanagement.config;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenStore {

    private final AtomicReference<String> latestToken = new AtomicReference<>();
    private final AtomicReference<List<String>> latestRoles = new AtomicReference<>(Collections.emptyList());

    public void store(String bearerToken) {
        latestToken.set(bearerToken);
    }

    public void storeRoles(List<String> roles) {
        latestRoles.set(roles != null ? roles : Collections.emptyList());
    }

    public String get() {
        return latestToken.get();
    }

    public List<String> getRoles() {
        return latestRoles.get();
    }
}
