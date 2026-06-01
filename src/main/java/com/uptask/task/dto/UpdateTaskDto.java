package com.uptask.task.dto;

import com.uptask.task.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskDto(
        @NotBlank @Size(min = 3, max = 255) String title,
        @Size(max = 5000) String description,
        TaskPriority priority,
        LocalDate dueDate
) {}
