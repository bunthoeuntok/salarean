package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for subject information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private UUID id;
    private String name;
    private String nameKhmer;
    private String code;
    private List<Integer> gradeLevels;
    private boolean isCore;
    private Integer displayOrder;
}
