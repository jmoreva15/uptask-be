package com.uptask.projectrole.repository;

import com.uptask.projectrole.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    List<ProjectRole> findAllByProjectId(Long projectId);

    Optional<ProjectRole> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByNameAndProjectId(String name, Long projectId);

    boolean existsByNameAndProjectIdAndIdNot(String name, Long projectId, Long excludeId);

    @Query("SELECT r FROM ProjectRole r WHERE r.project.id = :projectId AND r.system = true AND r.name = 'MANAGER'")
    Optional<ProjectRole> findManagerRoleByProjectId(@Param("projectId") Long projectId);
}
