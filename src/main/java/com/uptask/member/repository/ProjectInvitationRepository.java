package com.uptask.member.repository;

import com.uptask.member.entity.InvitationStatus;
import com.uptask.member.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    Optional<ProjectInvitation> findByTokenHash(String tokenHash);

    boolean existsByProjectIdAndEmailAndStatus(Long projectId, String email, InvitationStatus status);

    List<ProjectInvitation> findAllByProjectId(Long projectId);
}
