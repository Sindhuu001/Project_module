package com.example.projectmanagement.config;

import com.example.projectmanagement.scheduler.RmsPollingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class TokenCapturingFilter extends OncePerRequestFilter {

    private static final Set<String> MANAGER_ROLES = Set.of(
            "project_manager", "resource_manager", "hr_manager");

    private final TokenStore tokenStore;
    private final RmsPollingService rmsPollingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            List<String> incomingRoles = extractRolesFromJwt(authHeader);
            boolean wasManagerBefore = tokenStore.getRoles().stream()
                    .anyMatch(r -> MANAGER_ROLES.contains(r.toLowerCase().replace(" ", "_")));
            boolean isManagerNow = incomingRoles.stream()
                    .anyMatch(r -> MANAGER_ROLES.contains(r.toLowerCase().replace(" ", "_")));

            // Only replace the stored token if:
            // - the new request is from a manager (always keep the freshest manager token), OR
            // - no manager token was stored yet (accept any token until a manager logs in).
            // This prevents a general-user request from overwriting a valid manager token,
            // which would cause the scheduler to skip polling and leave stale employee IDs.
            if (isManagerNow || !wasManagerBefore) {
                tokenStore.store(authHeader);
                tokenStore.storeRoles(incomingRoles);
            }

            // Trigger an immediate poll when a manager-role user's token is captured
            // and the previous stored token was not from a manager (or no token existed).
            if (isManagerNow && !wasManagerBefore) {
                log.info("Manager role detected ({}), triggering immediate RMS poll", incomingRoles);
                Thread pollThread = new Thread(() -> {
                    try {
                        rmsPollingService.pollRmsResources();
                    } catch (Exception e) {
                        // logged inside pollRmsResources per project
                    }
                });
                pollThread.setDaemon(true);
                pollThread.setName("rms-manager-poll");
                pollThread.start();
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<String> extractRolesFromJwt(String bearerToken) {
        try {
            String jwt = bearerToken.substring(7); // strip "Bearer "
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return Collections.emptyList();

            // JWT payload is base64url-encoded — pad to multiple of 4
            String payload = parts[1];
            int mod = payload.length() % 4;
            if (mod != 0) payload = payload + "=".repeat(4 - mod);

            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            Map<String, Object> claims = objectMapper.readValue(decoded, new TypeReference<>() {});

            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?> roleList) {
                return roleList.stream()
                        .filter(r -> r instanceof String)
                        .map(r -> (String) r)
                        .toList();
            }
        } catch (Exception e) {
            log.debug("Could not extract roles from JWT: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
