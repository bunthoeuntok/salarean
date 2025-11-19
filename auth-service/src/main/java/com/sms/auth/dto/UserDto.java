package com.sms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String profilePhotoUrl;
    private String role;
    private UUID schoolId;
    private String preferredLanguage;
}
