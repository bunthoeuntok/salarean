package com.sms.auth.controller;

import com.sms.auth.dto.ApiResponse;
import com.sms.auth.dto.ChangePasswordRequest;
import com.sms.auth.dto.PhotoUploadResponse;
import com.sms.auth.dto.ProfileResponse;
import com.sms.auth.dto.UpdateProfileRequest;
import com.sms.auth.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile",
               description = "Retrieve profile information for the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUserProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        ProfileResponse profile = profileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile",
               description = "Update name, phone number, or language preference for the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile));
    }

    @PutMapping("/users/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password",
               description = "Change password with current password verification. Invalidates all other sessions and refresh tokens.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        // Extract token from "Bearer {token}"
        String accessToken = authHeader.substring(7);

        profileService.changePassword(userId, request, accessToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/users/me/photo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload profile photo",
               description = "Upload or update profile photo (JPG/PNG, max 5MB). Replaces existing photo.")
    public ResponseEntity<ApiResponse<PhotoUploadResponse>> uploadProfilePhoto(
            @RequestParam("photo") MultipartFile file,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        PhotoUploadResponse response = profileService.uploadProfilePhoto(userId, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
