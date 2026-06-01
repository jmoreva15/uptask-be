package com.uptask.comment.repository;

import com.uptask.comment.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);

    Optional<TaskComment> findByIdAndTaskId(Long id, Long taskId);
}
