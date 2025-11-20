package com.sms.auth.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // Min 8 chars, 1 upper, 1 lower, 1 number, 1 special
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*()_\\-]).{8,}$"
    );

    public boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public String getRequirementsMessage() {
        return "Password must be at least 8 characters with uppercase, " +
               "lowercase, number, and special character";
    }
}
