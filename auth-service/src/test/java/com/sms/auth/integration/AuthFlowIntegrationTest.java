package com.sms.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.auth.dto.BaseResponse;
import com.sms.auth.dto.LoginRequest;
import com.sms.auth.dto.RegisterRequest;
import com.sms.auth.repository.LoginAttemptRepository;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test for complete authentication flow.
 * Tests registration, login, and session management.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @AfterEach
    void cleanup() {
        sessionRepository.deleteAll();
        loginAttemptRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void completeAuthFlow_registerThenLogin_succeeds() throws Exception {
        // ========== Step 1: Register a new teacher ==========
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("teacher@school.edu.kh");
        registerRequest.setPhoneNumber("+85512345678");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setPreferredLanguage("en");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value("teacher@school.edu.kh"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+85512345678"))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.userId").exists())
            .andReturn();

        String registerResponseJson = registerResult.getResponse().getContentAsString();
        assertNotNull(registerResponseJson);
        assertTrue(registerResponseJson.contains("token"));

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail("teacher@school.edu.kh"));
        assertTrue(userRepository.existsByPhoneNumber("+85512345678"));

        // ========== Step 2: Login with email ==========
        LoginRequest loginWithEmail = new LoginRequest();
        loginWithEmail.setEmailOrPhone("teacher@school.edu.kh");
        loginWithEmail.setPassword("SecurePass123!");

        MvcResult loginEmailResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginWithEmail)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value("teacher@school.edu.kh"))
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn();

        String loginEmailToken = extractToken(loginEmailResult);
        assertNotNull(loginEmailToken);
        assertFalse(loginEmailToken.isEmpty());

        // ========== Step 3: Login with phone ==========
        LoginRequest loginWithPhone = new LoginRequest();
        loginWithPhone.setEmailOrPhone("+85512345678");
        loginWithPhone.setPassword("SecurePass123!");

        MvcResult loginPhoneResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginWithPhone)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+85512345678"))
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn();

        String loginPhoneToken = extractToken(loginPhoneResult);
        assertNotNull(loginPhoneToken);
        assertFalse(loginPhoneToken.isEmpty());

        // Verify sessions were created
        long sessionCount = sessionRepository.count();
        assertEquals(3, sessionCount); // 1 from register + 2 from logins

        // ========== Step 4: Verify duplicate registration fails ==========
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));

        // ========== Step 5: Verify invalid credentials fail ==========
        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setEmailOrPhone("teacher@school.edu.kh");
        invalidLogin.setPassword("WrongPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void rateLimiting_afterMultipleFailedAttempts_blocksLogin() throws Exception {
        // ========== Step 1: Register a teacher ==========
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("teacher2@school.edu.kh");
        registerRequest.setPhoneNumber("+85587654321");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setPreferredLanguage("en");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // ========== Step 2: Attempt 5 failed logins ==========
        LoginRequest failedLogin = new LoginRequest();
        failedLogin.setEmailOrPhone("teacher2@school.edu.kh");
        failedLogin.setPassword("WrongPassword!");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(failedLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }

        // ========== Step 3: 6th attempt should be rate limited ==========
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failedLogin)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));

        // ========== Step 4: Even correct password should be blocked ==========
        LoginRequest correctLogin = new LoginRequest();
        correctLogin.setEmailOrPhone("teacher2@school.edu.kh");
        correctLogin.setPassword("SecurePass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctLogin)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @SuppressWarnings("unchecked")
    private String extractToken(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        BaseResponse<?> response = objectMapper.readValue(json, BaseResponse.class);
        Object data = response.getData();
        if (data instanceof java.util.Map) {
            return (String) ((java.util.Map<String, Object>) data).get("token");
        }
        return null;
    }
}
