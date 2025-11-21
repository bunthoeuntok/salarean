package com.sms.auth.validation;

import com.sms.auth.dto.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class PasswordStrengthValidator {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");

    // Top 100 most common passwords (subset for demonstration)
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
            "password", "123456", "123456789", "12345678", "12345", "1234567", "password1",
            "123123", "1234567890", "000000", "abc123", "654321", "qwerty", "qwertyuiop",
            "111111", "222222", "333333", "444444", "555555", "666666", "777777", "888888",
            "999999", "iloveyou", "princess", "admin", "welcome", "monkey", "login",
            "sunshine", "master", "starwars", "hello", "freedom", "whatever", "dragon",
            "passw0rd", "password123", "qwerty123", "letmein", "trustno1", "football",
            "baseball", "superman", "batman", "michael", "jennifer", "ashley", "jessica",
            "thomas", "daniel", "nicole", "killer", "samsung", "secret", "charlie"
    ));

    public ValidationResult validate(String password) {
        if (password == null || password.length() < 8) {
            return invalid(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (!UPPERCASE.matcher(password).find()) {
            return invalid(ErrorCode.PASSWORD_MISSING_UPPERCASE);
        }

        if (!LOWERCASE.matcher(password).find()) {
            return invalid(ErrorCode.PASSWORD_MISSING_LOWERCASE);
        }

        if (!DIGIT.matcher(password).find()) {
            return invalid(ErrorCode.PASSWORD_MISSING_DIGIT);
        }

        if (!SPECIAL.matcher(password).find()) {
            return invalid(ErrorCode.PASSWORD_MISSING_SPECIAL);
        }

        if (isCommonPassword(password)) {
            return invalid(ErrorCode.PASSWORD_TOO_COMMON);
        }

        return valid();
    }

    private boolean isCommonPassword(String password) {
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }

    private ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    private ValidationResult invalid(ErrorCode errorCode) {
        return new ValidationResult(false, errorCode);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final ErrorCode errorCode;

        public ValidationResult(boolean valid, ErrorCode errorCode) {
            this.valid = valid;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return valid;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }
    }
}
