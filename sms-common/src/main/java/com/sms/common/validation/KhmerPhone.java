package com.sms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates Cambodia phone number format.
 *
 * Valid formats:
 * - +855 12 345 678  (with country code)
 * - +85512345678     (with country code, no spaces)
 * - 012 345 678      (without country code)
 * - 012345678        (without country code, no spaces)
 *
 * Rules:
 * - Country code: +855 or 0
 * - Operator code: 1-9 (first digit after country code)
 * - Total digits: 8-9 digits after country code
 *
 * Usage:
 * <pre>
 * {@code
 * public class User {
 *     @KhmerPhone
 *     private String phoneNumber;
 * }
 * }
 * </pre>
 *
 * Note: Use with @NotNull if field is required.
 * This annotation allows null/empty values.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = KhmerPhoneValidator.class)
@Documented
public @interface KhmerPhone {

    /**
     * Error message returned when validation fails.
     * Frontend will map this to localized message.
     */
    String message() default "INVALID_PHONE_FORMAT";

    /**
     * Validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Additional payload
     */
    Class<? extends Payload>[] payload() default {};
}
