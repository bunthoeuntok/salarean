package com.sms.auth.dto;

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

    @Pattern(regexp = "^(\\+855|0)[1-9]\\d{7,8}$",
             message = "Phone number must be in valid Cambodia format")
    private String phoneNumber;

    @Pattern(regexp = "^(en|km)$",
             message = "Preferred language must be 'en' or 'km'")
    private String preferredLanguage;
}
