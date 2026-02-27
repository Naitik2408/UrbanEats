package com.urbaneats.service;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.*;
import com.urbaneats.entity.Admin;
import com.urbaneats.entity.User;
import com.urbaneats.repository.AdminRepository;
import com.urbaneats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests OTP generation, OTP verification, JWT generation, and admin login.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private OtpService otpService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Admin testAdmin;
    private String testEmail;
    private String testOtp;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testOtp = "123456";
        testUser = TestDataBuilder.buildCustomerUser();
        testAdmin = TestDataBuilder.buildAdmin();
    }

    // ========== OTP Generation Tests ==========

    @Test
    @DisplayName("Should generate OTP successfully for email")
    void shouldGenerateOtpForEmail() {
        // Arrange
        OtpRequest request = TestDataBuilder.buildOtpRequest(testEmail);
        when(otpService.generateAndStoreOtp(testEmail)).thenReturn(testOtp);

        // Act
        OtpResponse response = authService.generateOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("OTP sent successfully");
        assertThat(response.getOtp()).isEqualTo(testOtp);
        verify(otpService, times(1)).generateAndStoreOtp(testEmail);
    }

    @Test
    @DisplayName("Should generate OTP successfully for phone")
    void shouldGenerateOtpForPhone() {
        // Arrange
        String phone = "+1234567890";
        OtpRequest request = new OtpRequest();
        request.setPhone(phone);
        when(otpService.generateAndStoreOtp(phone)).thenReturn(testOtp);

        // Act
        OtpResponse response = authService.generateOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("OTP sent successfully");
        verify(otpService, times(1)).generateAndStoreOtp(phone);
    }

    @Test
    @DisplayName("Should return null OTP in production mode")
    void shouldReturnNullOtpInProductionMode() {
        // Arrange
        OtpRequest request = TestDataBuilder.buildOtpRequest(testEmail);
        when(otpService.generateAndStoreOtp(testEmail)).thenReturn(null);

        // Act
        OtpResponse response = authService.generateOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("check your email/SMS");
        assertThat(response.getOtp()).isNull();
    }

    // ========== OTP Verification Tests ==========

    @Test
    @DisplayName("Should verify OTP and authenticate existing user")
    void shouldVerifyOtpAndAuthenticateExistingUser() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, testOtp);
        when(otpService.verifyOtp(testEmail, testOtp)).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser.getId(), testUser.getRole().name()))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.verifyOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getMessage()).contains("Authentication successful");
        
        verify(otpService, times(1)).verifyOtp(testEmail, testOtp);
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(userRepository, never()).save(any());
        verify(jwtService, times(1)).generateToken(testUser.getId(), "ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should verify OTP and create new user if not exists")
    void shouldVerifyOtpAndCreateNewUser() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, testOtp);
        when(otpService.verifyOtp(testEmail, testOtp)).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.verifyOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");
        
        verify(otpService, times(1)).verifyOtp(testEmail, testOtp);
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(anyLong(), eq("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid OTP")
    void shouldThrowExceptionForInvalidOtp() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, "wrong-otp");
        when(otpService.verifyOtp(testEmail, "wrong-otp")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid or expired OTP");
        
        verify(otpService, times(1)).verifyOtp(testEmail, "wrong-otp");
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for expired OTP")
    void shouldThrowExceptionForExpiredOtp() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, testOtp);
        when(otpService.verifyOtp(testEmail, testOtp)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    // ========== Admin Login Tests ==========

    @Test
    @DisplayName("Should authenticate admin with valid credentials")
    void shouldAuthenticateAdminWithValidCredentials() {
        // Arrange
        AdminLoginRequest request = TestDataBuilder.buildAdminLoginRequest("admin", "admin123");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("admin123", testAdmin.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testAdmin.getId(), testAdmin.getRole().name()))
                .thenReturn("admin-jwt-token");

        // Act
        AuthResponse response = authService.adminLogin(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("admin-jwt-token");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getUserId()).isEqualTo(testAdmin.getId());
        
        verify(adminRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, times(1)).matches("admin123", testAdmin.getPassword());
        verify(jwtService, times(1)).generateToken(testAdmin.getId(), "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException for non-existent admin")
    void shouldThrowExceptionForNonExistentAdmin() {
        // Arrange
        AdminLoginRequest request = TestDataBuilder.buildAdminLoginRequest("nonexistent", "password");
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.adminLogin(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Admin not found");
        
        verify(adminRepository, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for wrong admin password")
    void shouldThrowExceptionForWrongAdminPassword() {
        // Arrange
        AdminLoginRequest request = TestDataBuilder.buildAdminLoginRequest("admin", "wrongpassword");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("wrongpassword", testAdmin.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.adminLogin(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
        
        verify(adminRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, times(1)).matches("wrongpassword", testAdmin.getPassword());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    // ========== Role Assignment Tests ==========

    @Test
    @DisplayName("Should assign ROLE_CUSTOMER to new user")
    void shouldAssignCustomerRoleToNewUser() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, testOtp);
        when(otpService.verifyOtp(testEmail, testOtp)).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getRole()).isEqualTo(User.Role.ROLE_CUSTOMER);
            return testUser;
        });
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.verifyOtp(request);

        // Assert
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should generate token with correct role for customer")
    void shouldGenerateTokenWithCustomerRole() {
        // Arrange
        OtpVerifyRequest request = TestDataBuilder.buildOtpVerifyRequest(testEmail, testOtp);
        when(otpService.verifyOtp(testEmail, testOtp)).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser.getId(), "ROLE_CUSTOMER")).thenReturn("jwt-token");

        // Act
        authService.verifyOtp(request);

        // Assert
        verify(jwtService, times(1)).generateToken(testUser.getId(), "ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should generate token with correct role for admin")
    void shouldGenerateTokenWithAdminRole() {
        // Arrange
        AdminLoginRequest request = TestDataBuilder.buildAdminLoginRequest("admin", "admin123");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("admin123", testAdmin.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testAdmin.getId(), "ROLE_ADMIN")).thenReturn("admin-jwt-token");

        // Act
        authService.adminLogin(request);

        // Assert
        verify(jwtService, times(1)).generateToken(testAdmin.getId(), "ROLE_ADMIN");
    }
}
