package com.sms.auth.validation;

import com.sms.auth.config.SecurityProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // Min length from SecurityProperties, 1 upper, 1 lower, 1 number, 1 special
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*()_\\-]).{" +
        SecurityProperties.MIN_PASSWORD_LENGTH + ",}$"
    );

    public boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public String getRequirementsMessage() {
        return "Password must be at least " + SecurityProperties.MIN_PASSWORD_LENGTH +
               " characters with uppercase, lowercase, number, and special character";
    }
}
