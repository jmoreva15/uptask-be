package com.uptask.project.dto;

import java.time.LocalDateTime;

public record ProjectDto(
        Long id,
        String name,
        String description,
        String key,
        ProjectOwnerDto owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ProjectOwnerDto(Long id, String firstName, String lastName, String email) {}
}
