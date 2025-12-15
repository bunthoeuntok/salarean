package com.sms.student.dto;

import com.sms.student.enums.SchoolType;
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
    private SchoolType schoolType;
    private UUID provinceId;
    private String provinceName;
    private UUID districtId;
    private String districtName;
    private String principalName;
    private String principalGender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
