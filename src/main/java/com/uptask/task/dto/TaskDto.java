package com.uptask.task.dto;

import com.uptask.task.entity.TaskPriority;
import com.uptask.task.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserRef assignee,
        UserRef reporter,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record UserRef(Long id, String firstName, String lastName, String email) {}
}
