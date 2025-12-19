package com.sms.grade.exception;

import java.util.UUID;

public class ConfigNotFoundException extends RuntimeException {
    public ConfigNotFoundException(String message) {
        super(message);
    }

    public ConfigNotFoundException(UUID configId) {
        super("Configuration not found with ID: " + configId);
    }
}
