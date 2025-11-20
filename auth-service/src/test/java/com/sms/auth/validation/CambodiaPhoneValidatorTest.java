package com.sms.auth.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CambodiaPhoneValidatorTest {

    private CambodiaPhoneValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CambodiaPhoneValidator();
    }

    @Test
    void isValid_withValidCambodiaPhone_returnsTrue() {
        assertTrue(validator.isValid("+85512345678"));  // 8 digits
        assertTrue(validator.isValid("+855123456789")); // 9 digits
        assertTrue(validator.isValid("+85587654321"));
        assertTrue(validator.isValid("+85591234567"));
    }

    @Test
    void isValid_withInvalidPrefix_returnsFalse() {
        assertFalse(validator.isValid("+85612345678"));  // Wrong country code
        assertFalse(validator.isValid("85512345678"));    // Missing +
        assertFalse(validator.isValid("+85412345678"));   // Wrong prefix
    }

    @Test
    void isValid_withZeroAfter855_returnsFalse() {
        assertFalse(validator.isValid("+85501234567"));  // Starts with 0
        assertFalse(validator.isValid("+85502345678"));
    }

    @Test
    void isValid_withTooFewDigits_returnsFalse() {
        assertFalse(validator.isValid("+8551234567"));   // Only 7 digits
        assertFalse(validator.isValid("+855123456"));    // Only 6 digits
    }

    @Test
    void isValid_withTooManyDigits_returnsFalse() {
        assertFalse(validator.isValid("+8551234567890")); // 10 digits
        assertFalse(validator.isValid("+85512345678901")); // 11 digits
    }

    @Test
    void isValid_withNullPhone_returnsFalse() {
        assertFalse(validator.isValid(null));
    }

    @Test
    void isValid_withEmptyPhone_returnsFalse() {
        assertFalse(validator.isValid(""));
    }

    @Test
    void isValid_withSpaces_returnsFalse() {
        assertFalse(validator.isValid("+855 12 345 678"));
        assertFalse(validator.isValid("+855 123 456 78"));
    }

    @Test
    void isValid_withNonNumericCharacters_returnsFalse() {
        assertFalse(validator.isValid("+855-12-345-678"));
        assertFalse(validator.isValid("+855(12)345678"));
    }

    @Test
    void getRequirementsMessage_returnsDescriptiveMessage() {
        String message = validator.getRequirementsMessage();
        assertNotNull(message);
        assertTrue(message.contains("+855"));
        assertTrue(message.contains("Cambodia"));
    }
}
