package com.uptask.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberDto(
        @NotBlank @Email String email,
        @NotNull Long roleId
) {}
