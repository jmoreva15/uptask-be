package com.uptask.task.dto;

import com.uptask.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusDto(@NotNull TaskStatus status) {}
