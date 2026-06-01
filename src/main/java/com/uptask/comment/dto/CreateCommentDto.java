package com.uptask.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentDto(@NotBlank @Size(min = 1, max = 5000) String content) {}
