package io.github.supplierratingsoftware.supplierratingbackend.controller;

import io.github.supplierratingsoftware.supplierratingbackend.dto.api.LoginRequestDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.LoginResponseDto;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * The PAT is supplied in the {@code password} field of the request. It is validated
     * against openBIS, and the returned user id must match the submitted username.
     * @param loginRequest The login payload containing the username and the PAT.
     * @return {@link LoginResponseDto} with token and username on success,
     *         or HTTP 401 Unauthorized if the username does not match the PAT owner.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        // The PAT is expected to be in the password field of the login request
        String userPat = loginRequest.password();

        // Validate the PAT against openBIS and retrieve the associated user id
        String userIdFromOpenBis = openBisClient.validatePat(userPat);

        // Ensure that the user id from openBIS matches the username provided in the login request
        if (!userIdFromOpenBis.equalsIgnoreCase(loginRequest.username())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //  On success, return the PAT as the session token together with the user id.
        return ResponseEntity.ok(new LoginResponseDto(userPat, userIdFromOpenBis));
    }
}