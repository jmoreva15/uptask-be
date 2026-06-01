package com.uptask.projectrole.repository;

import com.uptask.projectrole.entity.ProjectPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectPermissionRepository extends JpaRepository<ProjectPermission, Long> {

    Optional<ProjectPermission> findByName(String name);

    List<ProjectPermission> findAllByIdIn(Set<Long> ids);

    List<ProjectPermission> findAllByNameIn(Set<String> names);
}
