package io.github.supplierratingsoftware.supplierratingbackend.controller;

import io.github.supplierratingsoftware.supplierratingbackend.dto.api.LoginRequestDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.LoginResponseDto;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication.
 * Validates a user's openBIS Personal Access Token (PAT) and confirms their identity.
 * basePath: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OpenBisClient openBisClient;

    /**
     * Authenticates a user by validating their Personal Access Token (PAT) against openBIS.
     * The PAT is supplied in the {@code password} field of the request.
     * <p>
     * No username is needed: a PAT belongs to exactly one openBIS user, and openBIS returns
     * that user's id when the PAT is validated. This id is sent back to the frontend.
     * </p>
     * @param loginRequest The login payload containing the PAT.
     * @return {@link LoginResponseDto} with the token and the username of the PAT owner.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        // The PAT is expected to be in the password field of the login request
        String userPat = loginRequest.password();

        // Validate the PAT against openBIS and retrieve the user id of the PAT owner
        String userIdFromOpenBis = openBisClient.validatePat(userPat);

        // On success, return the PAT as the session token together with the user id from openBIS.
        return ResponseEntity.ok(new LoginResponseDto(userPat, userIdFromOpenBis));
    }
}