package com.sms.auth.service;

import com.sms.auth.config.SecurityProperties;
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

    /**
     * Check if identifier is rate-limited
     * (more than MAX_LOGIN_ATTEMPTS failed attempts in last ACCOUNT_LOCK_DURATION_MINUTES)
     */
    public boolean isRateLimited(String identifier) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(SecurityProperties.ACCOUNT_LOCK_DURATION_MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(identifier, since);
        return failedAttempts >= SecurityProperties.MAX_LOGIN_ATTEMPTS;
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
