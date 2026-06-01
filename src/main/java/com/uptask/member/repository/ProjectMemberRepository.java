package com.uptask.member.repository;

import com.uptask.member.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findAllByProjectId(Long projectId);

    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    @Query("""
            SELECT COUNT(m) > 0 FROM ProjectMember m
            JOIN m.projectRole r
            JOIN r.permissions p
            WHERE m.project.id = :projectId
              AND m.user.id    = :userId
              AND p.name       = :permission
            """)
    boolean hasPermission(
            @Param("projectId")  Long projectId,
            @Param("userId")     Long userId,
            @Param("permission") String permission
    );

    boolean existsByProjectRoleId(Long projectRoleId);
}
