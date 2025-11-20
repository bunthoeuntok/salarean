package com.sms.auth.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CambodiaPhoneValidator {

    // Cambodia phone format: +855 followed by 8-9 digits starting with 1-9
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+855[1-9]\\d{7,8}$"
    );

    public boolean isValid(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public String getRequirementsMessage() {
        return "Phone number must be in Cambodia format: +855 XX XXX XXX";
    }
}
