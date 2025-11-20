package com.sms.auth.service;

import com.sms.auth.dto.AuthResponse;
import com.sms.auth.dto.LoginRequest;
import com.sms.auth.dto.RegisterRequest;
import com.sms.auth.exception.*;
import com.sms.auth.model.Session;
import com.sms.auth.model.User;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtTokenProvider;
import com.sms.auth.validation.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("teacher@school.edu.kh");
        registerRequest.setPhoneNumber("+85512345678");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setPreferredLanguage("en");

        loginRequest = new LoginRequest();
        loginRequest.setEmailOrPhone("teacher@school.edu.kh");
        loginRequest.setPassword("SecurePass123!");

        testUser = User.builder()
            .id(UUID.randomUUID())
            .email("teacher@school.edu.kh")
            .phoneNumber("+85512345678")
            .passwordHash("$2a$12$hashedPassword")
            .preferredLanguage("en")
            .build();

        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        lenient().when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    // ========== REGISTER TESTS ==========

    @Test
    void register_withValidData_createsUserAndSession() {
        // Arrange
        when(passwordValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString())).thenReturn("jwt-token");
        when(jwtTokenProvider.getJtiFromToken(anyString())).thenReturn("jti-123");
        when(sessionRepository.save(any(Session.class))).thenReturn(new Session());

        // Act
        AuthResponse response = authService.register(registerRequest, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getPhoneNumber(), response.getPhoneNumber());
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void register_withInvalidPassword_throwsInvalidPasswordException() {
        // Arrange
        when(passwordValidator.isValid(anyString())).thenReturn(false);
        when(passwordValidator.getRequirementsMessage()).thenReturn("Password requirements not met");

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> {
            authService.register(registerRequest, httpRequest);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsDuplicateEmailException() {
        // Arrange
        when(passwordValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> {
            authService.register(registerRequest, httpRequest);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicatePhone_throwsDuplicatePhoneException() {
        // Arrange
        when(passwordValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicatePhoneException.class, () -> {
            authService.register(registerRequest, httpRequest);
        });
        verify(userRepository, never()).save(any());
    }

    // ========== LOGIN TESTS ==========

    @Test
    void login_withValidEmail_succeeds() {
        // Arrange
        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString())).thenReturn("jwt-token");
        when(jwtTokenProvider.getJtiFromToken(anyString())).thenReturn("jti-123");
        when(sessionRepository.save(any(Session.class))).thenReturn(new Session());

        // Act
        AuthResponse response = authService.login(loginRequest, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("jwt-token", response.getToken());
        verify(rateLimitService).recordAttempt(anyString(), anyString(), eq(true), isNull());
    }

    @Test
    void login_withValidPhone_succeeds() {
        // Arrange
        loginRequest.setEmailOrPhone("+85512345678");
        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString())).thenReturn("jwt-token");
        when(jwtTokenProvider.getJtiFromToken(anyString())).thenReturn("jti-123");
        when(sessionRepository.save(any(Session.class))).thenReturn(new Session());

        // Act
        AuthResponse response = authService.login(loginRequest, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getPhoneNumber(), response.getPhoneNumber());
    }

    @Test
    void login_withInvalidCredentials_throwsInvalidCredentialsException() {
        // Arrange
        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest, httpRequest);
        });
        verify(rateLimitService).recordAttempt(anyString(), anyString(), eq(false), eq("INVALID_PASSWORD"));
    }

    @Test
    void login_withNonExistentUser_throwsInvalidCredentialsException() {
        // Arrange
        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest, httpRequest);
        });
        verify(rateLimitService).recordAttempt(anyString(), anyString(), eq(false), eq("INVALID_PASSWORD"));
    }

    @Test
    void login_whenRateLimited_throwsRateLimitExceededException() {
        // Arrange
        when(rateLimitService.isRateLimited(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RateLimitExceededException.class, () -> {
            authService.login(loginRequest, httpRequest);
        });
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findByPhoneNumber(any());
    }
}
