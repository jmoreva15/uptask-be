package com.uptask.projectrole.dto;

import java.util.Set;

public record ProjectRoleDto(
        Long id,
        String name,
        String description,
        boolean system,
        Set<PermissionDto> permissions
) {
    public record PermissionDto(Long id, String name, String description, String category) {}
}
