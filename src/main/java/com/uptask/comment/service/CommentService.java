package com.uptask.comment.service;

import com.uptask.activity.entity.ActivityType;
import com.uptask.activity.service.ActivityService;
import com.uptask.comment.dto.CommentDto;
import com.uptask.comment.dto.CreateCommentDto;
import com.uptask.comment.entity.TaskComment;
import com.uptask.comment.repository.TaskCommentRepository;
import com.uptask.common.exception.ApplicationException;
import com.uptask.security.principal.UserPrincipal;
import com.uptask.task.entity.Task;
import com.uptask.task.exception.TaskNotFoundException;
import com.uptask.task.repository.TaskRepository;
import com.uptask.user.entity.User;
import com.uptask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long projectId, Long taskId) {
        findTask(projectId, taskId);
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CommentDto addComment(Long projectId, Long taskId, CreateCommentDto dto) {
        Task task = findTask(projectId, taskId);
        User author = currentUser();

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(dto.content());

        comment = commentRepository.save(comment);
        activityService.record(task, author, ActivityType.COMMENTED, null, dto.content());

        return toDto(comment);
    }

    @Transactional
    public void deleteComment(Long projectId, Long taskId, Long commentId, boolean canDeleteAny) {
        Task task = findTask(projectId, taskId);
        User actor = currentUser();

        TaskComment comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                .orElseThrow(() -> new ApplicationException("Comment not found", HttpStatus.NOT_FOUND));

        boolean isOwn = comment.getAuthor().getId().equals(actor.getId());

        if (!isOwn && !canDeleteAny) {
            throw new ApplicationException("You can only delete your own comments", HttpStatus.FORBIDDEN);
        }

        activityService.record(task, actor, ActivityType.COMMENT_DELETED, null, null);
        commentRepository.delete(comment);
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private User currentUser() {
        Long userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private CommentDto toDto(TaskComment c) {
        return new CommentDto(
                c.getId(),
                c.getTask().getId(),
                new CommentDto.AuthorDto(c.getAuthor().getId(), c.getAuthor().getFirstName(),
                        c.getAuthor().getLastName(), c.getAuthor().getEmail()),
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
