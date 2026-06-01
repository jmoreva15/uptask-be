package com.uptask.comment.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long taskId,
        AuthorDto author,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AuthorDto(Long id, String firstName, String lastName, String email) {}
}
