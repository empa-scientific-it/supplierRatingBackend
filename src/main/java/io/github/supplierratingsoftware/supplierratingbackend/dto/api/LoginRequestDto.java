package io.github.supplierratingsoftware.supplierratingbackend.dto.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a login request.
 * Carries the credentials submitted from the login form.
 * <p>
 * No username is required: the Personal Access Token (PAT) belongs to exactly one openBIS user,
 * and openBIS returns that user when the PAT is validated.
 * </p>
 * @param password The user's openBIS Personal Access Token (PAT),
 *                 entered in the password field on the frontend (Mandatory).
 */
public record LoginRequestDto(
        @NotBlank(message = "Personal Access Token is required")
        String password
) {}