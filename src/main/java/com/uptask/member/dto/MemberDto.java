package com.uptask.member.dto;

import java.time.LocalDateTime;

public record MemberDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        RoleInfo role,
        LocalDateTime joinedAt
) {
    public record RoleInfo(Long id, String name) {}
}
