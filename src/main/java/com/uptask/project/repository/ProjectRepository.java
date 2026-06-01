package com.uptask.project.repository;

import com.uptask.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByKey(String key);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.owner.id = :userId
               OR EXISTS (
                   SELECT m FROM ProjectMember m
                   WHERE m.project = p AND m.user.id = :userId
               )
            ORDER BY p.createdAt DESC
            """)
    List<Project> findAllAccessibleByUser(@Param("userId") Long userId);
}
