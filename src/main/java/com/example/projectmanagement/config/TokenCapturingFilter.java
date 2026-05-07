package com.example.projectmanagement.config;

import com.example.projectmanagement.scheduler.RmsPollingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenCapturingFilter extends OncePerRequestFilter {

    private final TokenStore tokenStore;
    private final RmsPollingService rmsPollingService;

    public TokenCapturingFilter(TokenStore tokenStore, @Lazy RmsPollingService rmsPollingService) {
        this.tokenStore = tokenStore;
        this.rmsPollingService = rmsPollingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            boolean isFirstToken = tokenStore.get() == null;
            tokenStore.store(authHeader);

            // Trigger an immediate RMS poll on first user login
            if (isFirstToken) {
                Thread pollThread = new Thread(() -> {
                    try {
                        rmsPollingService.pollRmsResources();
                    } catch (Exception e) {
                        // logged inside pollRmsResources per project
                    }
                });
                pollThread.setDaemon(true);
                pollThread.setName("rms-initial-poll");
                pollThread.start();
            }
        }
        filterChain.doFilter(request, response);
    }
}
