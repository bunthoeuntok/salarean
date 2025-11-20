package com.sms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.auth.dto.AuthResponse;
import com.sms.auth.dto.LoginRequest;
import com.sms.auth.dto.RegisterRequest;
import com.sms.auth.exception.*;
import com.sms.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

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

        authResponse = AuthResponse.builder()
            .userId(UUID.randomUUID())
            .email("teacher@school.edu.kh")
            .phoneNumber("+85512345678")
            .preferredLanguage("en")
            .token("jwt-token-here")
            .createdAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();
    }

    // ========== REGISTER ENDPOINT TESTS ==========

    @Test
    void register_withValidData_returns200() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class), any())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value("teacher@school.edu.kh"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+85512345678"))
            .andExpect(jsonPath("$.data.token").value("jwt-token-here"));
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void register_withInvalidPhone_returns400() throws Exception {
        // Arrange
        registerRequest.setPhoneNumber("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void register_withDuplicateEmail_returns400() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class), any()))
            .thenThrow(new DuplicateEmailException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));
    }

    @Test
    void register_withDuplicatePhone_returns400() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class), any()))
            .thenThrow(new DuplicatePhoneException("Phone already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PHONE"));
    }

    @Test
    void register_withInvalidPassword_returns400() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class), any()))
            .thenThrow(new InvalidPasswordException("Password too weak"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_PASSWORD"));
    }

    // ========== LOGIN ENDPOINT TESTS ==========

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class), any())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.token").value("jwt-token-here"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class), any()))
            .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_withRateLimitExceeded_returns429() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class), any()))
            .thenThrow(new RateLimitExceededException("Too many attempts"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void login_withMissingEmailOrPhone_returns400() throws Exception {
        // Arrange
        loginRequest.setEmailOrPhone("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void login_withMissingPassword_returns400() throws Exception {
        // Arrange
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").exists());
    }
}
