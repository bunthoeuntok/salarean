package com.sms.auth.service;

import com.sms.auth.repository.LoginAttemptRepository;
import com.sms.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for cleaning up expired sessions and old login attempts.
 * Runs scheduled jobs to maintain database health.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupService {

    private final SessionRepository sessionRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    /**
     * Delete expired sessions every hour.
     * Sessions are considered expired when expires_at < current time.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = sessionRepository.deleteByExpiresAtBefore(now);

        if (deletedCount > 0) {
            log.info("Cleaned up {} expired sessions", deletedCount);
        }
    }

    /**
     * Delete old login attempts every day at 2 AM.
     * Retains login attempts for 7 years for audit compliance.
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    @Transactional
    public void cleanupOldLoginAttempts() {
        LocalDateTime sevenYearsAgo = LocalDateTime.now().minusYears(7);
        int deletedCount = loginAttemptRepository.deleteByAttemptedAtBefore(sevenYearsAgo);

        if (deletedCount > 0) {
            log.info("Cleaned up {} old login attempts (older than 7 years)", deletedCount);
        }
    }
}
