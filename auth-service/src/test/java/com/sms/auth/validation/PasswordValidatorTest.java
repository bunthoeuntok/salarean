package com.sms.auth.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
    }

    @Test
    void isValid_withValidPassword_returnsTrue() {
        assertTrue(validator.isValid("SecurePass123!"));
        assertTrue(validator.isValid("MyP@ssw0rd"));
        assertTrue(validator.isValid("Test@1234"));
    }

    @Test
    void isValid_withTooShortPassword_returnsFalse() {
        assertFalse(validator.isValid("Pwd1!"));  // Only 5 characters
        assertFalse(validator.isValid("Test@1"));  // Only 6 characters
    }

    @Test
    void isValid_withoutUppercase_returnsFalse() {
        assertFalse(validator.isValid("password123!"));
        assertFalse(validator.isValid("myp@ssw0rd"));
    }

    @Test
    void isValid_withoutLowercase_returnsFalse() {
        assertFalse(validator.isValid("PASSWORD123!"));
        assertFalse(validator.isValid("MYP@SSW0RD"));
    }

    @Test
    void isValid_withoutNumber_returnsFalse() {
        assertFalse(validator.isValid("SecurePass!"));
        assertFalse(validator.isValid("MyP@ssword"));
    }

    @Test
    void isValid_withoutSpecialCharacter_returnsFalse() {
        assertFalse(validator.isValid("SecurePass123"));
        assertFalse(validator.isValid("MyPassword0"));
    }

    @Test
    void isValid_withNullPassword_returnsFalse() {
        assertFalse(validator.isValid(null));
    }

    @Test
    void isValid_withEmptyPassword_returnsFalse() {
        assertFalse(validator.isValid(""));
    }

    @Test
    void isValid_withVariousSpecialCharacters_returnsTrue() {
        assertTrue(validator.isValid("Password1@"));
        assertTrue(validator.isValid("Password1#"));
        assertTrue(validator.isValid("Password1$"));
        assertTrue(validator.isValid("Password1%"));
        assertTrue(validator.isValid("Password1^"));
        assertTrue(validator.isValid("Password1&"));
        assertTrue(validator.isValid("Password1*"));
        assertTrue(validator.isValid("Password1("));
        assertTrue(validator.isValid("Password1)"));
        assertTrue(validator.isValid("Password1_"));
        assertTrue(validator.isValid("Password1-"));
    }

    @Test
    void getRequirementsMessage_returnsDescriptiveMessage() {
        String message = validator.getRequirementsMessage();
        assertNotNull(message);
        assertTrue(message.contains("8"));
        assertTrue(message.contains("uppercase"));
        assertTrue(message.contains("lowercase"));
        assertTrue(message.contains("number"));
        assertTrue(message.contains("special"));
    }
}
