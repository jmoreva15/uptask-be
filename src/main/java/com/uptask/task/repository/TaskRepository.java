package com.uptask.task.repository;

import com.uptask.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
            ORDER BY t.createdAt DESC
            """)
    Page<Task> findAllByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
