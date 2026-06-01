package com.uptask.projectrole.service;

import com.uptask.common.exception.ApplicationException;
import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.entity.Project;
import com.uptask.project.exception.ProjectNotFoundException;
import com.uptask.project.repository.ProjectRepository;
import com.uptask.projectrole.dto.CreateProjectRoleDto;
import com.uptask.projectrole.dto.ProjectRoleDto;
import com.uptask.projectrole.dto.UpdateProjectRoleDto;
import com.uptask.projectrole.entity.ProjectPermission;
import com.uptask.projectrole.entity.ProjectRole;
import com.uptask.projectrole.exception.ProjectRoleNotFoundException;
import com.uptask.projectrole.mapper.ProjectRoleMapper;
import com.uptask.projectrole.repository.ProjectPermissionRepository;
import com.uptask.projectrole.repository.ProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectRoleService {

    private final ProjectRoleRepository roleRepository;
    private final ProjectPermissionRepository permissionRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleMapper mapper;

    @Transactional
    public ProjectRole createManagerRole(Project project) {
        List<ProjectPermission> allPermissions = permissionRepository.findAll();

        ProjectRole managerRole = new ProjectRole();
        managerRole.setProject(project);
        managerRole.setName("MANAGER");
        managerRole.setDescription("Full access to project");
        managerRole.setSystem(true);
        managerRole.setPermissions(new HashSet<>(allPermissions));

        return roleRepository.save(managerRole);
    }

    @Transactional(readOnly = true)
    public List<ProjectRoleDto> getRoles(Long projectId) {
        return roleRepository.findAllByProjectId(projectId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public ProjectRoleDto createRole(Long projectId, CreateProjectRoleDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (roleRepository.existsByNameAndProjectId(dto.name(), projectId)) {
            throw new ApplicationException("A role with this name already exists in the project", HttpStatus.CONFLICT);
        }

        Set<ProjectPermission> permissions = resolvePermissions(dto.permissionIds());

        ProjectRole role = new ProjectRole();
        role.setProject(project);
        role.setName(dto.name());
        role.setDescription(dto.description());
        role.setPermissions(permissions);

        return mapper.toDto(roleRepository.save(role));
    }

    @Transactional
    public ProjectRoleDto updateRole(Long projectId, Long roleId, UpdateProjectRoleDto dto) {
        ProjectRole role = roleRepository.findByIdAndProjectId(roleId, projectId)
                .orElseThrow(() -> new ProjectRoleNotFoundException(roleId));

        if (role.isSystem()) {
            throw new ApplicationException("System roles cannot be modified", HttpStatus.FORBIDDEN);
        }

        if (roleRepository.existsByNameAndProjectIdAndIdNot(dto.name(), projectId, roleId)) {
            throw new ApplicationException("A role with this name already exists in the project", HttpStatus.CONFLICT);
        }

        role.setName(dto.name());
        role.setDescription(dto.description());
        role.setPermissions(resolvePermissions(dto.permissionIds()));

        return mapper.toDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long projectId, Long roleId) {
        ProjectRole role = roleRepository.findByIdAndProjectId(roleId, projectId)
                .orElseThrow(() -> new ProjectRoleNotFoundException(roleId));

        if (role.isSystem()) {
            throw new ApplicationException("System roles cannot be deleted", HttpStatus.FORBIDDEN);
        }

        if (memberRepository.existsByProjectRoleId(roleId)) {
            throw new ApplicationException("Cannot delete a role that is assigned to members", HttpStatus.CONFLICT);
        }

        roleRepository.delete(role);
    }

    public ProjectRole getManagerRole(Long projectId) {
        return roleRepository.findManagerRoleByProjectId(projectId)
                .orElseThrow(() -> new ApplicationException("Manager role not found", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public ProjectRole getRoleForProject(Long roleId, Long projectId) {
        return roleRepository.findByIdAndProjectId(roleId, projectId)
                .orElseThrow(() -> new ProjectRoleNotFoundException(roleId));
    }

    private Set<ProjectPermission> resolvePermissions(Set<Long> ids) {
        List<ProjectPermission> found = permissionRepository.findAllByIdIn(ids);
        if (found.size() != ids.size()) {
            throw new ApplicationException("One or more permission IDs are invalid", HttpStatus.BAD_REQUEST);
        }
        return new HashSet<>(found);
    }
}
