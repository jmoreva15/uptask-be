package com.uptask.member.service;

import com.uptask.common.exception.ApplicationException;
import com.uptask.common.util.HashUtil;
import com.uptask.common.util.OtpUtil;
import com.uptask.email.service.EmailService;
import com.uptask.member.dto.InviteMemberDto;
import com.uptask.member.entity.InvitationStatus;
import com.uptask.member.entity.ProjectInvitation;
import com.uptask.member.entity.ProjectMember;
import com.uptask.member.exception.InvitationNotFoundException;
import com.uptask.member.repository.ProjectInvitationRepository;
import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.entity.Project;
import com.uptask.project.exception.ProjectNotFoundException;
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

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleService roleService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void invite(Long projectId, InviteMemberDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        ProjectRole role = roleService.getRoleForProject(dto.roleId(), projectId);

        userRepository.findByEmail(dto.email()).ifPresent(user -> {
            if (memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
                throw new ApplicationException("This user is already a member of the project", HttpStatus.CONFLICT);
            }
        });

        if (invitationRepository.existsByProjectIdAndEmailAndStatus(projectId, dto.email(), InvitationStatus.PENDING)) {
            throw new ApplicationException("A pending invitation already exists for this email", HttpStatus.CONFLICT);
        }

        User inviter = currentUser();
        String rawToken = OtpUtil.generateSecureToken(32);

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setEmail(dto.email());
        invitation.setProjectRole(role);
        invitation.setTokenHash(HashUtil.sha256(rawToken));
        invitation.setInvitedBy(inviter);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));

        invitationRepository.save(invitation);

        emailService.sendProjectInvitationEmail(
                dto.email(),
                inviter.getFirstName() + " " + inviter.getLastName(),
                project.getName(),
                rawToken,
                INVITATION_EXPIRY_DAYS
        );
    }

    @Transactional
    public void accept(String rawToken) {
        User currentUser = currentUser();
        ProjectInvitation invitation = findValidInvitation(rawToken);

        if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new ApplicationException("This invitation was sent to a different email address", HttpStatus.FORBIDDEN);
        }

        if (memberRepository.existsByProjectIdAndUserId(invitation.getProject().getId(), currentUser.getId())) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
            return;
        }

        ProjectMember member = new ProjectMember();
        member.setProject(invitation.getProject());
        member.setUser(currentUser);
        member.setProjectRole(invitation.getProjectRole());
        member.setInvitedBy(invitation.getInvitedBy());
        member.setJoinedAt(LocalDateTime.now());
        memberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void decline(String rawToken) {
        User currentUser = currentUser();
        ProjectInvitation invitation = findValidInvitation(rawToken);

        if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new ApplicationException("This invitation was sent to a different email address", HttpStatus.FORBIDDEN);
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    private ProjectInvitation findValidInvitation(String rawToken) {
        String tokenHash = HashUtil.sha256(rawToken);
        ProjectInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvitationNotFoundException::new);

        if (!invitation.isPending()) {
            throw new ApplicationException("This invitation is no longer valid", HttpStatus.GONE);
        }

        return invitation;
    }

    private User currentUser() {
        Long userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
