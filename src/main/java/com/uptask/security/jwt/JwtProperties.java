package com.uptask.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String privateKeyLocation,
        String publicKeyLocation,
        @DefaultValue("15m") Duration accessTokenExpiration,
        @DefaultValue("7d") Duration refreshTokenExpiration,
        @DefaultValue("15m") Duration passwordResetTokenExpiration
) {}
