package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoUploadResponse {

    private String photoUrl;
    private String thumbnailUrl;
    private LocalDateTime uploadedAt;
}
