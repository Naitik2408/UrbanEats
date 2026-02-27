package com.urbaneats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.*;
import com.urbaneats.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthController.
 * Uses @WebMvcTest for lightweight controller testing with mocked service layer.
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private OtpRequest otpRequest;
    private OtpVerifyRequest otpVerifyRequest;
    private AdminLoginRequest adminLoginRequest;

    @BeforeEach
    void setUp() {
        otpRequest = TestDataBuilder.buildOtpRequest("test@example.com");
        otpVerifyRequest = TestDataBuilder.buildOtpVerifyRequest("test@example.com", "123456");
        adminLoginRequest = TestDataBuilder.buildAdminLoginRequest("admin", "admin123");
    }

    // ========== OTP Generation Tests ==========

    @Test
    @DisplayName("Should generate OTP successfully")
    void shouldGenerateOtpSuccessfully() throws Exception {
        // Arrange
        OtpResponse otpResponse = new OtpResponse("OTP sent successfully", "123456");
        when(authService.generateOtp(any(OtpRequest.class))).thenReturn(otpResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/get-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"))
                .andExpect(jsonPath("$.otp").value("123456"));

        verify(authService, times(1)).generateOtp(any(OtpRequest.class));
    }

    @Test
    @DisplayName("Should return 400 for invalid email format")
    void shouldReturn400ForInvalidEmail() throws Exception {
        // Arrange
        OtpRequest invalidRequest = new OtpRequest();
        invalidRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/get-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).generateOtp(any());
    }

    // ========== OTP Verification Tests ==========

    @Test
    @DisplayName("Should verify OTP and return JWT token")
    void shouldVerifyOtpAndReturnToken() throws Exception {
        // Arrange
        AuthResponse authResponse = new AuthResponse(
                "jwt-token",
                "ROLE_CUSTOMER",
                1L,
                "Authentication successful"
        );
        when(authService.verifyOtp(any(OtpVerifyRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("Authentication successful"));

        verify(authService, times(1)).verifyOtp(any(OtpVerifyRequest.class));
    }

    @Test
    @DisplayName("Should return 401 for invalid OTP")
    void shouldReturn401ForInvalidOtp() throws Exception {
        // Arrange
        when(authService.verifyOtp(any(OtpVerifyRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid or expired OTP"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpVerifyRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failed"));

        verify(authService, times(1)).verifyOtp(any(OtpVerifyRequest.class));
    }

    @Test
    @DisplayName("Should return 400 for missing OTP")
    void shouldReturn400ForMissingOtp() throws Exception {
        // Arrange
        OtpVerifyRequest invalidRequest = new OtpVerifyRequest();
        invalidRequest.setIdentifier("test@example.com");
        // otp is null

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyOtp(any());
    }

    // ========== Admin Login Tests ==========

    @Test
    @DisplayName("Should authenticate admin successfully")
    void shouldAuthenticateAdminSuccessfully() throws Exception {
        // Arrange
        AuthResponse authResponse = new AuthResponse(
                "admin-jwt-token",
                "ROLE_ADMIN",
                1L,
                "Authentication successful"
        );
        when(authService.adminLogin(any(AdminLoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(authService, times(1)).adminLogin(any(AdminLoginRequest.class));
    }

    @Test
    @DisplayName("Should return 404 for non-existent admin")
    void shouldReturn404ForNonExistentAdmin() throws Exception {
        // Arrange
        when(authService.adminLogin(any(AdminLoginRequest.class)))
                .thenThrow(new EntityNotFoundException("Admin not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));

        verify(authService, times(1)).adminLogin(any(AdminLoginRequest.class));
    }

    @Test
    @DisplayName("Should return 401 for wrong admin password")
    void shouldReturn401ForWrongAdminPassword() throws Exception {
        // Arrange
        when(authService.adminLogin(any(AdminLoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failed"));

        verify(authService, times(1)).adminLogin(any(AdminLoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 for missing username")
    void shouldReturn400ForMissingUsername() throws Exception {
        // Arrange
        AdminLoginRequest invalidRequest = new AdminLoginRequest();
        invalidRequest.setPassword("password");
        // username is null

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).adminLogin(any());
    }

    @Test
    @DisplayName("Should return 400 for missing password")
    void shouldReturn400ForMissingPassword() throws Exception {
        // Arrange
        AdminLoginRequest invalidRequest = new AdminLoginRequest();
        invalidRequest.setUsername("admin");
        // password is null

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).adminLogin(any());
    }
}
