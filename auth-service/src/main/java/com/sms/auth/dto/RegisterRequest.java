package com.sms.auth.dto;

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
    @Pattern(regexp = "^\\+855[1-9]\\d{7,8}$",
             message = "Phone must be Cambodia format (+855 XX XXX XXX)")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(regexp = "^(en|km)$", message = "Language must be 'en' or 'km'")
    private String preferredLanguage = "en";
}
