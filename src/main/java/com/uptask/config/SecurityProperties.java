package com.uptask.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @DefaultValue("5") int maxFailedLoginAttempts,
        @DefaultValue("30") int accountLockDurationMinutes,
        @DefaultValue("10") int otpExpirationMinutes,
        @DefaultValue("3") int otpMaxAttempts
) {}
