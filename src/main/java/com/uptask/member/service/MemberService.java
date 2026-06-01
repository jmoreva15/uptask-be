package com.uptask.member.service;

import com.uptask.common.exception.ApplicationException;
import com.uptask.member.dto.AssignRoleDto;
import com.uptask.member.dto.MemberDto;
import com.uptask.member.entity.ProjectMember;
import com.uptask.member.exception.MemberNotFoundException;
import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.entity.Project;
import com.uptask.project.exception.ProjectNotFoundException;
import com.uptask.project.repository.ProjectRepository;
import com.uptask.projectrole.entity.ProjectRole;
import com.uptask.projectrole.service.ProjectRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleService roleService;

    @Transactional(readOnly = true)
    public List<MemberDto> getMembers(Long projectId) {
        return memberRepository.findAllByProjectId(projectId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getOwner().getId().equals(userId)) {
            throw new ApplicationException("The project owner cannot be removed", HttpStatus.FORBIDDEN);
        }

        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new MemberNotFoundException();
        }

        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Transactional
    public MemberDto assignRole(Long projectId, Long userId, AssignRoleDto dto) {
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(MemberNotFoundException::new);

        Project project = member.getProject();
        if (project.getOwner().getId().equals(userId)) {
            throw new ApplicationException("Cannot change the owner's role", HttpStatus.FORBIDDEN);
        }

        ProjectRole newRole = roleService.getRoleForProject(dto.roleId(), projectId);
        member.setProjectRole(newRole);

        return toDto(memberRepository.save(member));
    }

    private MemberDto toDto(ProjectMember m) {
        return new MemberDto(
                m.getUser().getId(),
                m.getUser().getFirstName(),
                m.getUser().getLastName(),
                m.getUser().getEmail(),
                new MemberDto.RoleInfo(m.getProjectRole().getId(), m.getProjectRole().getName()),
                m.getJoinedAt()
        );
    }

}
