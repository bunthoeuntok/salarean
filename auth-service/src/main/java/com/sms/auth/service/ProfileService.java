package com.sms.auth.service;

import com.sms.auth.dto.ChangePasswordRequest;
import com.sms.common.dto.ErrorCode;
import com.sms.auth.dto.PhotoUploadResponse;
import com.sms.auth.dto.ProfileResponse;
import com.sms.auth.dto.UpdateProfileRequest;
import com.sms.auth.exception.ProfileUpdateException;
import com.sms.auth.model.User;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtTokenProvider;
import com.sms.auth.validation.PasswordStrengthValidator;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    private static final Pattern CAMBODIA_PHONE_PATTERN = Pattern.compile("^(\\+855|0)[1-9]\\d{7,8}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordStrengthValidator passwordStrengthValidator;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PhotoStorageService photoStorageService;

    /**
     * Get user profile by user ID
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileUpdateException(ErrorCode.UNAUTHORIZED, "User not found"));

        return mapToProfileResponse(user);
    }

    /**
     * Update user profile with validations
     */
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileUpdateException(ErrorCode.UNAUTHORIZED, "User not found"));

        // Track changes for audit logging
        StringBuilder changes = new StringBuilder();

        // Update name (trim and validate max length)
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            if (trimmedName.length() > 255) {
                throw new ProfileUpdateException(ErrorCode.VALIDATION_ERROR, "Name must not exceed 255 characters");
            }
            if (!trimmedName.equals(user.getName())) {
                changes.append("name, ");
                user.setName(trimmedName);
            }
        }

        // Update phone number (validate format and uniqueness)
        if (request.getPhoneNumber() != null) {
            String phoneNumber = request.getPhoneNumber().trim();

            // Validate Cambodia phone format
            if (!CAMBODIA_PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new ProfileUpdateException(ErrorCode.INVALID_PHONE_FORMAT,
                        "Phone number must be in valid Cambodia format");
            }

            // Check uniqueness (exclude current user)
            if (!phoneNumber.equals(user.getPhoneNumber())) {
                userRepository.findByPhoneNumberAndIdNot(phoneNumber, userId)
                        .ifPresent(existingUser -> {
                            throw new ProfileUpdateException(ErrorCode.DUPLICATE_PHONE,
                                    "This phone number is already registered");
                        });
                changes.append("phoneNumber, ");
                user.setPhoneNumber(phoneNumber);
            }
        }

        // Update preferred language (validate 'en' or 'km')
        if (request.getPreferredLanguage() != null) {
            String language = request.getPreferredLanguage().toLowerCase();
            if (!language.equals("en") && !language.equals("km")) {
                throw new ProfileUpdateException(ErrorCode.VALIDATION_ERROR,
                        "Preferred language must be 'en' or 'km'");
            }
            if (!language.equals(user.getPreferredLanguage())) {
                changes.append("preferredLanguage, ");
                user.setPreferredLanguage(language);
            }
        }

        // Save changes
        user = userRepository.save(user);

        // Audit logging
        if (changes.length() > 0) {
            String changedFields = changes.substring(0, changes.length() - 2); // Remove trailing ", "
            logger.info("Profile updated - User ID: {}, Fields: {}, Timestamp: {}",
                    userId, changedFields, java.time.LocalDateTime.now());
        }

        return mapToProfileResponse(user);
    }

    /**
     * Change user password with current password verification and session invalidation
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request, String currentAccessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileUpdateException(ErrorCode.UNAUTHORIZED, "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("Password change failed - incorrect current password for user: {}", userId);
            throw new ProfileUpdateException(ErrorCode.INVALID_PASSWORD,
                    "Current password is incorrect");
        }

        // Validate new password strength
        PasswordStrengthValidator.ValidationResult validationResult =
                passwordStrengthValidator.validate(request.getNewPassword());

        if (!validationResult.isValid()) {
            logger.warn("Password change failed - weak password for user: {}. Error: {}",
                    userId, validationResult.getErrorCode());
            throw new ProfileUpdateException(ErrorCode.WEAK_PASSWORD,
                    "New password does not meet security requirements");
        }

        // Update password hash
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Get current session JTI from access token
        String currentJti = jwtTokenProvider.getJtiFromToken(currentAccessToken);

        // Invalidate all other sessions (keep current session)
        int sessionsDeleted = sessionRepository.deleteByUserIdAndTokenJtiNot(userId, currentJti);

        // Revoke all refresh tokens (for security, revoke ALL tokens - user must re-login on other devices)
        tokenService.revokeAllUserTokens(userId);

        // Audit logging
        logger.info("Password changed successfully - User ID: {}, Sessions invalidated: {}, Timestamp: {}",
                userId, sessionsDeleted, java.time.LocalDateTime.now());
    }

    /**
     * Upload profile photo with validation
     */
    @Transactional
    public PhotoUploadResponse uploadProfilePhoto(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileUpdateException(ErrorCode.UNAUTHORIZED, "User not found"));

        // Validate photo (size, format, content type)
        photoStorageService.validatePhoto(file);

        // Save photo to filesystem and get URL
        String photoUrl = photoStorageService.savePhoto(userId, file);

        // Update user profile
        LocalDateTime uploadedAt = LocalDateTime.now();
        user.setProfilePhotoUrl(photoUrl);
        user.setProfilePhotoUploadedAt(uploadedAt);
        userRepository.save(user);

        // Audit logging
        logger.info("Profile photo uploaded - User ID: {}, Photo URL: {}, File size: {} bytes, Timestamp: {}",
                userId, photoUrl, file.getSize(), uploadedAt);

        return PhotoUploadResponse.builder()
                .photoUrl(photoUrl)
                .uploadedAt(uploadedAt)
                .build();
    }

    /**
     * Map User entity to ProfileResponse DTO
     */
    private ProfileResponse mapToProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .preferredLanguage(user.getPreferredLanguage())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .profilePhotoUploadedAt(user.getProfilePhotoUploadedAt())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
