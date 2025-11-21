package com.sms.auth.controller;

import com.sms.auth.dto.*;
import com.sms.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirement(name = "")  // Public endpoint - no auth required
    @Operation(summary = "Register a new teacher", description = "Register a new teacher with email, phone, and password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @SecurityRequirement(name = "")  // Public endpoint - no auth required
    @Operation(summary = "Teacher login", description = "Authenticate teacher with email/phone and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "")  // Public endpoint - no auth required
    @Operation(summary = "Refresh access token",
               description = "Exchange refresh token for new access and refresh tokens (token rotation)")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        RefreshTokenResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")  // Requires authentication
    @Operation(summary = "Logout", description = "Invalidate access token and revoke all refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        // Extract token from "Bearer {token}"
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
