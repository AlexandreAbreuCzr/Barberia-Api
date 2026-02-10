package com.alexandre.Barbearia_Api.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Stream;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = parseOrigins(allowedOrigins);
        if (origins.isEmpty()) {
            origins = List.of(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://localhost:3000",
                    "http://127.0.0.1:3000",
                    "https://alexandreabreuczr.github.io"
            );
        }

        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .filter(item -> !item.equalsIgnoreCase("*"))
                .filter(item -> !item.equalsIgnoreCase("null"))
                .toList();
    }
}
