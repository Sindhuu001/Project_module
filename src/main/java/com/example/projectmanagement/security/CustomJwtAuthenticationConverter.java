package com.example.projectmanagement.security;

import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.entity.User.UserRole;
import com.example.projectmanagement.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final UserRepository userRepository;

    public CustomJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        System.out.println("JWT email claim: " + jwt.getClaimAsString("email"));

        String name = jwt.getClaimAsString("name");

        if (email == null || name == null) {
            throw new RuntimeException("JWT does not contain required claims: email or name");
        }

        // Auto-create user if not present (and skip unused variable warning)
        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole(UserRole.DEVELOPER); // Default fallback role
            return userRepository.save(newUser);
        });

        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        // ✅ Fully parameterized type-safe collection
        Collection<? extends GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

        // ✅ Set email as principal
        return new JwtAuthenticationToken(jwt, authorities, email);
    }
}
