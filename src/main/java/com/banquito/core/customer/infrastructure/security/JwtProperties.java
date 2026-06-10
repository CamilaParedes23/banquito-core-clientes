package com.banquito.core.customer.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "banquito.security.jwt")
public record JwtProperties(
        String issuer,
        String secret
) {}
