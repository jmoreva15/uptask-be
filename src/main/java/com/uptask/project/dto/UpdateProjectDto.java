package com.uptask.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectDto(
        @NotBlank @Size(min = 3, max = 255) String name,
        @Size(max = 2000) String description
) {}
