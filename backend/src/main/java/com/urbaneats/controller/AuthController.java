package com.urbaneats.controller;

import com.urbaneats.dto.*;
import com.urbaneats.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 * Handles customer OTP-based authentication and admin login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Generate and send OTP to customer.
     * POST /api/auth/get-otp
     *
     * @param request OTP request with email or phone
     * @return OTP response
     */
    @PostMapping("/get-otp")
    public ResponseEntity<OtpResponse> getOtp(@Valid @RequestBody OtpRequest request) {
        OtpResponse response = authService.generateOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP and authenticate customer.
     * POST /api/auth/verify-otp
     *
     * @param request OTP verify request
     * @return Authentication response with JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin login with username and password.
     * POST /api/auth/admin/login
     *
     * @param request Admin login request
     * @return Authentication response with JWT token
     */
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        AuthResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(response);
    }
}
