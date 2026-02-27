package com.urbaneats.service;

import com.urbaneats.exception.WeakJwtSecretException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * Tests JWT generation, validation, claims extraction, and secret validation.
 */
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private String validSecret;
    private String weakSecret;
    private long expiration;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        validSecret = "test-secret-key-for-jwt-minimum-32-characters-long";
        weakSecret = "short";
        expiration = 3600000L; // 1 hour
    }

    // ========== Secret Validation Tests ==========

    @Test
    @DisplayName("Should validate JWT secret successfully when strong enough")
    void shouldValidateStrongSecret() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act & Assert
        assertThatCode(() -> jwtService.validateJwtSecret())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when JWT secret is too weak")
    void shouldThrowExceptionForWeakSecret() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", weakSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateJwtSecret())
                .isInstanceOf(WeakJwtSecretException.class)
                .hasMessageContaining("JWT secret must be at least 32 characters");
    }

    @Test
    @DisplayName("Should throw exception when JWT secret is null")
    void shouldThrowExceptionForNullSecret() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", null);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateJwtSecret())
                .isInstanceOf(WeakJwtSecretException.class);
    }

    // ========== Token Generation Tests ==========

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        Long userId = 1L;
        String role = "ROLE_CUSTOMER";

        // Act
        String token = jwtService.generateToken(userId, role);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate token with correct userId")
    void shouldGenerateTokenWithCorrectUserId() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        Long userId = 42L;
        String role = "ROLE_CUSTOMER";

        // Act
        String token = jwtService.generateToken(userId, role);
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should generate token with correct role")
    void shouldGenerateTokenWithCorrectRole() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        Long userId = 1L;
        String role = "ROLE_ADMIN";

        // Act
        String token = jwtService.generateToken(userId, role);
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        String token1 = jwtService.generateToken(1L, "ROLE_CUSTOMER");
        String token2 = jwtService.generateToken(2L, "ROLE_CUSTOMER");

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }

    // ========== Token Validation Tests ==========

    @Test
    @DisplayName("Should validate correct token successfully")
    void shouldValidateCorrectToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject tampered token")
    void shouldRejectTamperedToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        // Act
        boolean isValid = jwtService.validateToken(tamperedToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        boolean isValid = jwtService.validateToken("invalid.token.here");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject null token")
    void shouldRejectNullToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        boolean isValid = jwtService.validateToken(null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        boolean isValid = jwtService.validateToken("");

        // Assert
        assertThat(isValid).isFalse();
    }

    // ========== Claims Extraction Tests ==========

    @Test
    @DisplayName("Should extract username (userId) from token")
    void shouldExtractUsername() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        Long userId = 123L;
        String token = jwtService.generateToken(userId, "ROLE_CUSTOMER");

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Should extract userId claim from token")
    void shouldExtractUserIdClaim() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        Long userId = 999L;
        String token = jwtService.generateToken(userId, "ROLE_CUSTOMER");

        // Act
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should extract role claim from token")
    void shouldExtractRoleClaim() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        String role = "ROLE_ADMIN";
        String token = jwtService.generateToken(1L, role);

        // Act
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void shouldExtractExpiration() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");

        // Act
        var expirationDate = jwtService.extractExpiration(token);

        // Assert
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate.getTime()).isGreaterThan(System.currentTimeMillis());
    }

    // ========== Token Expiration Tests ==========

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L); // Expired
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");

        // Reset to normal expiration for validation
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should validate non-expired token")
    void shouldValidateNonExpiredToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    // ========== Role-Specific Token Tests ==========

    @Test
    @DisplayName("Should generate valid customer token")
    void shouldGenerateValidCustomerToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        String token = jwtService.generateToken(1L, "ROLE_CUSTOMER");

        // Assert
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should generate valid admin token")
    void shouldGenerateValidAdminToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expiration);

        // Act
        String token = jwtService.generateToken(1L, "ROLE_ADMIN");

        // Assert
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }
}
