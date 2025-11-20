package com.sms.auth.service;

import com.sms.auth.model.LoginAttempt;
import com.sms.auth.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_MINUTES = 15;

    /**
     * Check if identifier is rate-limited (more than 5 failed attempts in last 15 minutes)
     */
    public boolean isRateLimited(String identifier) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(identifier, since);
        return failedAttempts >= MAX_ATTEMPTS;
    }

    /**
     * Record a login attempt (successful or failed).
     * Uses REQUIRES_NEW to ensure this is saved even if the outer transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(String identifier, String ipAddress, boolean success, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
            .identifier(identifier)
            .ipAddress(ipAddress)
            .success(success)
            .failureReason(failureReason)
            .build();
        loginAttemptRepository.save(attempt);
    }
}
