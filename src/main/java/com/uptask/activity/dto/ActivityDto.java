package com.uptask.activity.dto;

import com.uptask.activity.entity.ActivityType;

import java.time.LocalDateTime;

public record ActivityDto(
        Long id,
        ActivityType actionType,
        String oldValue,
        String newValue,
        ActorDto actor,
        LocalDateTime createdAt
) {
    public record ActorDto(Long id, String firstName, String lastName) {}
}
