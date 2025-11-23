package com.sms.auth.dto;

import com.sms.common.validation.KhmerPhone;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @KhmerPhone
    private String phoneNumber;

    @Pattern(regexp = "^(en|km)$",
             message = "Preferred language must be 'en' or 'km'")
    private String preferredLanguage;
}
