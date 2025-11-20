package com.sms.auth.service;

import com.sms.auth.model.LoginAttempt;
import com.sms.auth.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @InjectMocks
    private RateLimitService rateLimitService;

    private String testIdentifier;
    private String testIp;

    @BeforeEach
    void setUp() {
        testIdentifier = "teacher@school.edu.kh";
        testIp = "192.168.1.1";
    }

    @Test
    void isRateLimited_withLessThan5FailedAttempts_returnsFalse() {
        // Arrange
        when(loginAttemptRepository.countFailedAttemptsSince(anyString(), any(LocalDateTime.class)))
            .thenReturn(4L);

        // Act
        boolean result = rateLimitService.isRateLimited(testIdentifier);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRateLimited_withExactly5FailedAttempts_returnsTrue() {
        // Arrange
        when(loginAttemptRepository.countFailedAttemptsSince(anyString(), any(LocalDateTime.class)))
            .thenReturn(5L);

        // Act
        boolean result = rateLimitService.isRateLimited(testIdentifier);

        // Assert
        assertTrue(result);
    }

    @Test
    void isRateLimited_withMoreThan5FailedAttempts_returnsTrue() {
        // Arrange
        when(loginAttemptRepository.countFailedAttemptsSince(anyString(), any(LocalDateTime.class)))
            .thenReturn(10L);

        // Act
        boolean result = rateLimitService.isRateLimited(testIdentifier);

        // Assert
        assertTrue(result);
    }

    @Test
    void recordAttempt_withSuccessfulLogin_savesSuccessRecord() {
        // Arrange
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        rateLimitService.recordAttempt(testIdentifier, testIp, true, null);

        // Assert
        verify(loginAttemptRepository).save(argThat(attempt ->
            attempt.getIdentifier().equals(testIdentifier) &&
            attempt.getIpAddress().equals(testIp) &&
            attempt.getSuccess() &&
            attempt.getFailureReason() == null
        ));
    }

    @Test
    void recordAttempt_withFailedLogin_savesFailureRecord() {
        // Arrange
        String failureReason = "INVALID_PASSWORD";
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        rateLimitService.recordAttempt(testIdentifier, testIp, false, failureReason);

        // Assert
        verify(loginAttemptRepository).save(argThat(attempt ->
            attempt.getIdentifier().equals(testIdentifier) &&
            attempt.getIpAddress().equals(testIp) &&
            !attempt.getSuccess() &&
            attempt.getFailureReason().equals(failureReason)
        ));
    }

    @Test
    void isRateLimited_checksAttemptsWithin15Minutes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        when(loginAttemptRepository.countFailedAttemptsSince(anyString(), any(LocalDateTime.class)))
            .thenReturn(3L);

        // Act
        rateLimitService.isRateLimited(testIdentifier);

        // Assert
        verify(loginAttemptRepository).countFailedAttemptsSince(
            eq(testIdentifier),
            argThat(time -> {
                // Verify the time is approximately 15 minutes ago
                LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
                return time.isAfter(fifteenMinutesAgo.minusSeconds(5)) &&
                       time.isBefore(fifteenMinutesAgo.plusSeconds(5));
            })
        );
    }
}
