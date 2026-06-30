package com.stockflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "stockflow.security")
public record SecurityProperties(
        Jwt jwt,
        BootstrapAdmin bootstrapAdmin
) {
    public record Jwt(
            String secret,
            String issuer,
            Duration ttl
    ) {
    }

    public record BootstrapAdmin(
            String name,
            String email,
            String password
    ) {
    }
}
