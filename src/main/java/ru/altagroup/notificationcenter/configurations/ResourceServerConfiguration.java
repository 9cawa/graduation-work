package ru.altagroup.notificationcenter.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfiguration {

    private final String[] swaggerUris = {"/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs",
            "/webjars/**", "/view/**", "/v3/api-docs**", "/v3/api-docs/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(swaggerUris).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(converter());
        return http.build();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("sub");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object realmClaim = jwt.getClaim("realm_access");
            Map<String, List<String>> realm_access = objectMapper.convertValue(realmClaim, Map.class);
            List<String> roles = realm_access.get("roles");
            Object scopeClaim = jwt.getClaim("scope");
            String scope = objectMapper.convertValue(scopeClaim, String.class);
            String[] scopes = scope.split(" ");
            List<GrantedAuthority> rolesList = roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role)).toList();
            List<GrantedAuthority> scopesList = Arrays.stream(scopes).map(s -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + s)).toList();
            return Stream.concat(rolesList.stream(), scopesList.stream()).collect(Collectors.toList());
        });
        return converter;
    }
}
