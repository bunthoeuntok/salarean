package com.sms.auth.controller;

import com.sms.auth.dto.*;
import com.sms.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new teacher", description = "Register a new teacher with email, phone, and password")
    public ResponseEntity<BaseResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Teacher login", description = "Authenticate teacher with email/phone and password")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
