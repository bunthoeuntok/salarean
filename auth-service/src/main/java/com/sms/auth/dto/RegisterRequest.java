package com.sms.auth.dto;

import com.sms.common.validation.KhmerPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @KhmerPhone
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(regexp = "^(en|km)$", message = "Language must be 'en' or 'km'")
    private String preferredLanguage = "en";
}
