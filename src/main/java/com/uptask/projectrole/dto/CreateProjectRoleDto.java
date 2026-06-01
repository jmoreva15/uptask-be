package com.uptask.projectrole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateProjectRoleDto(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 255) String description,
        @NotNull Set<Long> permissionIds
) {}
