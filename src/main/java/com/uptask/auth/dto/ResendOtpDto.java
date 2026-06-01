package com.uptask.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpDto(
        @NotBlank @Email String email
) {}
