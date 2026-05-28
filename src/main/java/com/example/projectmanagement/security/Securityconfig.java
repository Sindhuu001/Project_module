package com.example.projectmanagement.security;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class Securityconfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/public/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**")
                .permitAll()
                .anyRequest().authenticated())

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(customAuthenticationEntryPoint())
            )

            .cors(withDefaults())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

@Bean
public AuthenticationEntryPoint customAuthenticationEntryPoint() {

    return (request, response, authException) -> {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = "Unauthorized";

        Throwable cause = authException;

        while (cause != null) {

            if (cause instanceof JwtException ||
                cause instanceof IllegalStateException) {

                String errorMessage = cause.getMessage();

                if (errorMessage != null &&
                    errorMessage.toLowerCase().contains("expired")) {

                    message = "Token has expired";
                } else {
                    message = errorMessage;
                }

                break;
            }

            cause = cause.getCause();
        }

        Map<String, Object> error = new HashMap<>();

        error.put("status", 401);
        error.put("message", message);

        new ObjectMapper().writeValue(response.getOutputStream(), error);
    };
}
    private JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<GrantedAuthority> authorities = new ArrayList<>();

            List<String> roles = jwt.getClaimAsStringList("roles");

            System.out.println("*********************************roles:" + roles);

            if (roles != null) {
                for (String role : roles) {

                    authorities.add(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                            "ROLE_" + role.replace(" ", "_").toUpperCase()
                        )
                    );
                }
            }

            return authorities;
        });

        return converter;
    }
}