package com.uptask.security.project;

import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.repository.ProjectRepository;
import com.uptask.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralized project-level authorization service.
 * Used via @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'PERMISSION')").
 * Identity is always resolved from the SecurityContext — never from request parameters.
 */
@Service("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurityService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Long projectId, String permission) {
        Long userId = currentUserId();
        if (projectRepository.existsByIdAndOwnerId(projectId, userId)) {
            return true;
        }
        return memberRepository.hasPermission(projectId, userId, permission);
    }

    @Transactional(readOnly = true)
    public boolean isMember(Long projectId) {
        Long userId = currentUserId();
        return projectRepository.existsByIdAndOwnerId(projectId, userId)
                || memberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long projectId) {
        return projectRepository.existsByIdAndOwnerId(projectId, currentUserId());
    }

    private Long currentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }
}
