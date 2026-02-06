package com.iemr.mmu.utils.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("swagger")
public class SwaggerSecurityConfig {

    @Bean
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println(">>> SwaggerSecurityConfig is ACTIVE <<<");
        // CSRF is disabled here because these endpoints are only used for API documentation/testing.
        // No authentication or state-changing operations are exposed, so disabling CSRF is safe.
        http
            .securityMatcher("/v3/api-docs/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
