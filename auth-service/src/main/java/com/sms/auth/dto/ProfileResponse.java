package com.sms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID id;
    private String email;
    private String phoneNumber;
    private String name;
    private String preferredLanguage;
    private String profilePhotoUrl;
    private LocalDateTime profilePhotoUploadedAt;
    private String accountStatus;
    private LocalDateTime createdAt;
}
