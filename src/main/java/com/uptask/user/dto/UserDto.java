package com.uptask.user.dto;

import com.uptask.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        UserStatus status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {}
