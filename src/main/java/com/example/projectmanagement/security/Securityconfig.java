package com.example.projectmanagement.security;
 
import java.util.ArrayList;
import java.util.List;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;
 
@Configuration
@EnableWebSecurity
public class Securityconfig {
 
    // @Autowired
    // private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/public/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            ).cors(withDefaults())
            .csrf(csrf -> csrf.disable());
           
        return http.build();
    }
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            System.out.println("roles:" + roles);
            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.replace(" ", "_").toUpperCase()));
                }
            }
//            List<String> permissions = jwt.getClaimAsStringList("permissions");
//            if (permissions != null) {
//                for (String perm : permissions) {
//                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(perm));
//                }
//            }
            return authorities;
        });
        return converter;
    }
}
 
// package com.example.projectmanagement.security;
 
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
// @Configuration
// @EnableWebSecurity
// public class Securityconfig {
 
//     private final PermissionCheckFilter permissionCheckFilter;
 
//     public Securityconfig(PermissionCheckFilter permissionCheckFilter) {
//         this.permissionCheckFilter = permissionCheckFilter;
//     }
 
//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             // ✅ Disable CSRF (typical for stateless REST APIs)
//             .csrf(csrf -> csrf.disable())
           
//             // ✅ Stateless session (no server-side sessions)
//             .sessionManagement(session ->
//                 session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           
//             // ✅ Authorization rules
//             .authorizeHttpRequests(auth -> auth
//                 // Public endpoints (no authentication required)
//                 .requestMatchers(
//                     "/auth/**",
//                     "/public/**",
//                     "/swagger-ui/**",
//                     "/swagger-ui.html",
//                     "/swagger-resources/**",
//                     "/v3/api-docs/**",
//                     "/webjars/**",
//                     "/actuator/health",
//                     "/error"
//                 ).permitAll()
               
//                 // All other endpoints - allow through (PermissionCheckFilter handles authorization)
//                 .anyRequest().permitAll()
//             )
           
//             // ✅ Add permission filter AFTER authentication check
//             // This ensures only authenticated requests reach your permission filter
//             // .addFilterAfter(permissionCheckFilter, UsernamePasswordAuthenticationFilter.class)
//             .oauth2ResourceServer(oauth2 -> oauth2.jwt())
//             // ✅ Disable form login (REST API doesn't need it)
//             .formLogin(form -> form.disable())
           
//             // ✅ Disable HTTP Basic (using Bearer tokens instead)
//             .httpBasic(basic -> basic.disable());
 
//         return http.build();
//     }
// }