package com.uptask.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDto(
        @NotBlank @Email(message = "Must be a valid email address")
        String email,

        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character"
        )
        String password,

        @NotBlank @Size(min = 1, max = 100, message = "First name must not be blank")
        String firstName,

        @NotBlank @Size(min = 1, max = 100, message = "Last name must not be blank")
        String lastName,

        @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone must be a valid number")
        String phone
) {}
