package com.uptask.project.service;

import com.uptask.common.exception.ApplicationException;
import com.uptask.member.entity.ProjectMember;
import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.dto.CreateProjectDto;
import com.uptask.project.dto.ProjectDto;
import com.uptask.project.dto.UpdateProjectDto;
import com.uptask.project.entity.Project;
import com.uptask.project.exception.ProjectNotFoundException;
import com.uptask.project.mapper.ProjectMapper;
import com.uptask.project.repository.ProjectRepository;
import com.uptask.projectrole.entity.ProjectRole;
import com.uptask.projectrole.service.ProjectRoleService;
import com.uptask.security.principal.UserPrincipal;
import com.uptask.user.entity.User;
import com.uptask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectRoleService projectRoleService;
    private final UserRepository userRepository;
    private final ProjectMapper mapper;

    @Transactional(readOnly = true)
    public List<ProjectDto> getMyProjects() {
        Long userId = currentUserId();
        return projectRepository.findAllAccessibleByUser(userId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDto getProject(Long id) {
        Long userId = currentUserId();
        Project project = findAccessibleProject(id, userId);
        return mapper.toDto(project);
    }

    @Transactional
    public ProjectDto createProject(CreateProjectDto dto) {
        if (projectRepository.existsByKey(dto.key())) {
            throw new ApplicationException("Project key already in use", HttpStatus.CONFLICT);
        }

        User owner = currentUser();

        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setKey(dto.key());
        project.setOwner(owner);

        project = projectRepository.save(project);

        ProjectRole managerRole = projectRoleService.createManagerRole(project);
        addOwnerAsMember(project, owner, managerRole);

        return mapper.toDto(project);
    }

    @Transactional
    public ProjectDto updateProject(Long id, UpdateProjectDto dto) {
        Long userId = currentUserId();
        Project project = findAccessibleProject(id, userId);

        project.setName(dto.name());
        project.setDescription(dto.description());

        return mapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        Long userId = currentUserId();
        if (!projectRepository.existsByIdAndOwnerId(id, userId)) {
            throw new ApplicationException("Only the project owner can delete the project", HttpStatus.FORBIDDEN);
        }
        projectRepository.deleteById(id);
    }

    private void addOwnerAsMember(Project project, User owner, ProjectRole managerRole) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(owner);
        member.setProjectRole(managerRole);
        member.setJoinedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    private Project findAccessibleProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isMember = memberRepository.existsByProjectIdAndUserId(id, userId);

        if (!isOwner && !isMember) {
            throw new ProjectNotFoundException(id);
        }

        return project;
    }

    private Long currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    private User currentUser() {
        Long userId = currentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
