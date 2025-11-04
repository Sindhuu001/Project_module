package com.example.projectmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionCheckFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckFilter.class);

    @Value("${external.auth.url}")
    private String externalAuthUrl;

    private final RestTemplate restTemplate;

    public PermissionCheckFilter() {
        // Configure RestTemplate with timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20000); // 20 seconds connect timeout
        factory.setReadTimeout(20000);    // 20 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ Skip public/internal endpoints only
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Extract Bearer token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid Authorization header");
            return;
        }

        String tokenValue = token.substring(7).trim();
        if (tokenValue.isEmpty()) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Empty authorization token");
            return;
        }

        // 🔹 Call external permission check for EVERY request
        try {
            Map<String, Object> permissionResult = checkPermissionExternal(token, path, method);

            Boolean allowed = (Boolean) permissionResult.get("allowed");
            
            if (Boolean.TRUE.equals(allowed)) {
                // ✅ Permission granted - continue to controller
                logger.debug("Permission granted for {} {}", method, path);
                filterChain.doFilter(request, response);
            } else {
                // ❌ Permission explicitly denied
                String errorMessage = (String) permissionResult.getOrDefault("message", "Access denied");
                logger.warn("Permission denied for {} {}: {}", method, path, errorMessage);
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, errorMessage);
            }

        }  catch (HttpClientErrorException e) {

            logger.warn("Permission service returned client error: {}", e.getResponseBodyAsString());
            writeJsonError(response, e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
        writeJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                   "Permission service timeout or unreachable: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            // ❌ External service returned 5xx
            logger.error("Permission service error {}: {}", e.getStatusCode(), e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_BAD_GATEWAY,
                    "Permission service error");
            
        } catch (Exception e) {
    logger.warn("Permission service unreachable, skipping check in dev: {}", e.getMessage());
    filterChain.doFilter(request, response);
    }

    }

    /**
     * Call external FastAPI to check permission
     */
    private Map<String, Object> checkPermissionExternal(String token, String path, String method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("path", path);
        requestBody.put("method", method);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // This will throw HttpClientErrorException if external API returns 4xx
        ResponseEntity<Map> permissionResponse = restTemplate.exchange(
                externalAuthUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = permissionResponse.getBody();
        if (body == null) {
            body = new HashMap<>();
            body.put("allowed", false);
            body.put("message", "Empty response from permission service");
        }
        
        return body;
    }

    /**
     * Extract error message from HTTP exception response
     */
    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            // Try to parse JSON error message
            if (responseBody.contains("\"message\"")) {
                int start = responseBody.indexOf("\"message\"") + 11;
                int end = responseBody.indexOf("\"", start);
                if (end > start) {
                    return responseBody.substring(start, end);
                }
            }
            if (responseBody.contains("\"error\"")) {
                int start = responseBody.indexOf("\"error\"") + 9;
                int end = responseBody.indexOf("\"", start);
                if (end > start) {
                    return responseBody.substring(start, end);
                }
            }
            return responseBody.isEmpty() ? "Access denied" : responseBody;
        } catch (Exception ex) {
            return "Access denied";
        }
    }

    /**
     * Skip permission checks ONLY for public/internal endpoints
     */
    private boolean shouldSkip(String path) {
        return path.startsWith("/auth")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/public")
                || path.startsWith("/error")
                || path.startsWith("/actuator")
                || path.startsWith("/health");
    }

    /**
     * Write JSON error response
     */
    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String escapedMessage = message.replace("\"", "\\\"").replace("\n", "\\n");
        response.getWriter().write("{\"error\":\"" + escapedMessage + "\"}");
    }
}