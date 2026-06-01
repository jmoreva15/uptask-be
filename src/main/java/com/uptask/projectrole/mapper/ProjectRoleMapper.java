package com.uptask.projectrole.mapper;

import com.uptask.projectrole.dto.ProjectRoleDto;
import com.uptask.projectrole.entity.ProjectRole;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProjectRoleMapper {

    public ProjectRoleDto toDto(ProjectRole role) {
        var permissions = role.getPermissions().stream()
                .map(p -> new ProjectRoleDto.PermissionDto(p.getId(), p.getName(), p.getDescription(), p.getCategory()))
                .collect(Collectors.toSet());
        return new ProjectRoleDto(role.getId(), role.getName(), role.getDescription(), role.isSystem(), permissions);
    }
}
