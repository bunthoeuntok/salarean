package com.sms.student.dto;

import com.sms.student.enums.SchoolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolResponse {

    private UUID id;
    private String name;
    private String nameKhmer;
    private String address;

    // NEW: Foreign key IDs
    private UUID provinceId;
    private UUID districtId;

    // Enriched fields for display
    private String provinceName;
    private String districtName;

    // OLD: Deprecated VARCHAR fields (for backward compatibility)
    @Deprecated
    private String province;
    @Deprecated
    private String district;

    private SchoolType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
