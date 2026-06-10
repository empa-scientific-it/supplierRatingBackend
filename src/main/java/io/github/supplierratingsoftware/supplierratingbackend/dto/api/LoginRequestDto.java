package io.github.supplierratingsoftware.supplierratingbackend.dto.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a login request.
 * Carries the credentials submitted from the login form.
 * @param username The user's openBIS username (Mandatory).
 * @param password The user's openBIS Personal Access Token (PAT),
 *                 entered in the password field on the frontend (Mandatory).
 */
public record LoginRequestDto(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Personal Access Token is required")
        String password
) {}