package com.sms.student.dto;

import com.sms.student.enums.SchoolType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolRequest {

    @NotBlank(message = "School name is required")
    @Size(max = 255, message = "School name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Khmer name must not exceed 255 characters")
    private String nameKhmer;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Province is required")
    private UUID provinceId;

    @NotNull(message = "District is required")
    private UUID districtId;

    @NotNull(message = "School type is required")
    private SchoolType type;
}
