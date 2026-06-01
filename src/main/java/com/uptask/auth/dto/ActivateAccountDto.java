package com.uptask.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6, message = "OTP must be 6 digits") String otp
) {}
