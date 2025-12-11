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
public class TeacherSchoolResponse {

    private UUID id;
    private UUID userId;
    private UUID schoolId;
    private String schoolName;
    private String principalName;
    private String principalGender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
