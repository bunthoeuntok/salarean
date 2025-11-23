package com.sms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for Cambodia phone number format.
 *
 * Validates phone numbers according to Cambodia telecom standards:
 * - Country code: +855 or 0
 * - Operator codes: 1-9 (Cellcard, Smart, Metfone, Seatel, etc.)
 * - Format: 8-9 digits after country code
 *
 * Valid examples:
 * - +855 12 345 678
 * - +85512345678
 * - 012 345 678
 * - 012345678
 * - 093456789
 */
public class KhmerPhoneValidator implements ConstraintValidator<KhmerPhone, String> {

    /**
     * Cambodia phone number pattern
     *
     * Pattern breakdown:
     * - ^              : Start of string
     * - (\\+855|0)     : Country code (+855) or local (0)
     * - [1-9]          : Operator code (1-9)
     * - \\d{7,8}       : 7-8 more digits
     * - $              : End of string
     *
     * Allows optional spaces/hyphens for readability, which are removed before validation
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+855|0)[1-9]\\d{7,8}$");

    @Override
    public void initialize(KhmerPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Null or empty values are considered valid
        // Use @NotNull or @NotBlank for required fields
        if (phone == null || phone.isBlank()) {
            return true;
        }

        // Remove spaces, hyphens, and parentheses for validation
        String normalizedPhone = phone.replaceAll("[\\s\\-()]", "");

        // Validate against pattern
        return PHONE_PATTERN.matcher(normalizedPhone).matches();
    }
}
