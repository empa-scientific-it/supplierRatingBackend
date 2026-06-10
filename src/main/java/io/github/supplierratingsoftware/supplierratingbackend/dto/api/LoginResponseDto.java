package io.github.supplierratingsoftware.supplierratingbackend.dto.api;

/**
 * Represents a login response returned to the frontend.
 * Contains the session token (the validated openBIS PAT) and the authenticated username.
 * @param token    The session token used for subsequent openBIS requests (the validated PAT).
 * @param username The authenticated user's username.
 */
public record LoginResponseDto(
        String token,
        String username
) {}