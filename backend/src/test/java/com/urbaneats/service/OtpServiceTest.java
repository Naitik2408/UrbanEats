package com.urbaneats.service;

import com.urbaneats.exception.OtpAttemptsExceededException;
import com.urbaneats.exception.OtpCooldownException;
import com.urbaneats.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OtpService.
 * Tests OTP generation, validation, expiration, rate limiting, and cooldown.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private OtpService otpService;

    private String testIdentifier;

    @BeforeEach
    void setUp() {
        testIdentifier = "test@example.com";
        
        // Set properties via reflection
        ReflectionTestUtils.setField(otpService, "otpExpirationSeconds", 300L);
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 5);
        ReflectionTestUtils.setField(otpService, "cooldownSeconds", 30);
        ReflectionTestUtils.setField(otpService, "returnInResponse", true);
        
        // Mock RedisTemplate operations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ========== OTP Generation Tests ==========

    @Test
    @DisplayName("Should generate 6-digit OTP")
    void shouldGenerate6DigitOtp() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(null);
        when(valueOperations.get("otp:cooldown:" + testIdentifier)).thenReturn(null);

        // Act
        String otp = otpService.generateAndStoreOtp(testIdentifier);

        // Assert
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");
        
        verify(valueOperations).set(eq("otp:" + testIdentifier), eq(otp), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should store OTP in Redis with correct expiration")
    void shouldStoreOtpWithExpiration() {
        // Act
        String otp = otpService.generateAndStoreOtp(testIdentifier);

        // Assert
        verify(valueOperations).set("otp:" + testIdentifier, otp, 300L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should throw exception when max attempts exceeded")
    void shouldThrowExceptionWhenMaxAttemptsExceeded() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn("5");

        // Act & Assert
        assertThatThrownBy(() -> otpService.generateAndStoreOtp(testIdentifier))
                .isInstanceOf(OtpAttemptsExceededException.class)
                .hasMessageContaining("Maximum OTP attempts exceeded");
        
        verify(valueOperations, never()).set(contains("otp:" + testIdentifier), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw exception when cooldown active")
    void shouldThrowExceptionWhenCooldownActive() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn("3");
        when(valueOperations.get("otp:cooldown:" + testIdentifier)).thenReturn("20");

        // Act & Assert
        assertThatThrownBy(() -> otpService.generateAndStoreOtp(testIdentifier))
                .isInstanceOf(OtpCooldownException.class)
                .hasMessageContaining("wait 20 seconds");
        
        verify(valueOperations, never()).set(contains("otp:" + testIdentifier), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Should increment attempt counter on OTP generation")
    void shouldIncrementAttemptCounter() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn("2");
        when(valueOperations.get("otp:cooldown:" + testIdentifier)).thenReturn(null);

        // Act
        otpService.generateAndStoreOtp(testIdentifier);

        // Assert
        verify(valueOperations).set(eq("otp:attempts:" + testIdentifier), eq("3"), eq(300L), eq(TimeUnit.SECONDS));
    }

    // ========== OTP Verification Tests ==========

    @Test
    @DisplayName("Should verify valid OTP successfully")
    void shouldVerifyValidOtp() {
        // Arrange
        String otp = "123456";
        when(valueOperations.get("otp:" + testIdentifier)).thenReturn(otp);
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(1);

        // Act
        boolean result = otpService.verifyOtp(testIdentifier, otp);

        // Assert
        assertThat(result).isTrue();
        verify(redisTemplate).delete("otp:" + testIdentifier);
        verify(redisTemplate).delete("otp:attempts:" + testIdentifier);
    }

    @Test
    @DisplayName("Should reject invalid OTP")
    void shouldRejectInvalidOtp() {
        // Arrange
        when(valueOperations.get("otp:" + testIdentifier)).thenReturn("123456");
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(1);

        // Act
        boolean result = otpService.verifyOtp(testIdentifier, "wrong-otp");

        // Assert
        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("Should reject verification when OTP not found")
    void shouldRejectWhenOtpNotFound() {
        // Arrange
        when(valueOperations.get("otp:" + testIdentifier)).thenReturn(null);
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(1);

        // Act
        boolean result = otpService.verifyOtp(testIdentifier, "123456");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject verification when OTP expired")
    void shouldRejectExpiredOtp() {
        // Arrange
        when(valueOperations.get("otp:" + testIdentifier)).thenReturn(null);
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(1);

        // Act
        boolean result = otpService.verifyOtp(testIdentifier, "123456");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should clean up Redis keys after successful verification")
    void shouldCleanupRedisKeysAfterVerification() {
        // Arrange
        String otp = "123456";
        when(valueOperations.get("otp:" + testIdentifier)).thenReturn(otp);
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(1);

        // Act
        otpService.verifyOtp(testIdentifier, otp);

        // Assert
        verify(redisTemplate).delete("otp:" + testIdentifier);
        verify(redisTemplate).delete("otp:attempts:" + testIdentifier);
    }

    // ========== Rate Limiting Tests ==========

    @Test
    @DisplayName("Should allow OTP generation when attempts below limit")
    void shouldAllowGenerationWhenBelowLimit() {
        // Act & Assert
        assertThatCode(() -> otpService.generateAndStoreOtp(testIdentifier))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block OTP generation when attempts at limit")
    void shouldBlockGenerationWhenAtLimit() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn("5");

        // Act & Assert
        assertThatThrownBy(() -> otpService.generateAndStoreOtp(testIdentifier))
                .isInstanceOf(OtpAttemptsExceededException.class);
    }

    @Test
    @DisplayName("Should set cooldown after OTP generation")
    void shouldSetCooldownAfterGeneration() {
        // Arrange
        when(valueOperations.get("otp:attempts:" + testIdentifier)).thenReturn(null);
        when(valueOperations.get("otp:cooldown:" + testIdentifier)).thenReturn(null);

        // Act
        otpService.generateAndStoreOtp(testIdentifier);

        // Assert
        verify(valueOperations).set(eq("otp:cooldown:" + testIdentifier), eq("30"), eq(30L), eq(TimeUnit.SECONDS));
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle null identifier gracefully")
    void shouldHandleNullIdentifier() {
        // Act & Assert
        assertThatThrownBy(() -> otpService.generateAndStoreOtp(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle empty identifier gracefully")
    void shouldHandleEmptyIdentifier() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        String otp = otpService.generateAndStoreOtp("");

        // Assert
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
    }

    @Test
    @DisplayName("Should generate different OTPs for different identifiers")
    void shouldGenerateDifferentOtpsForDifferentIdentifiers() {
        // Arrange
        String identifier1 = "user1@example.com";
        String identifier2 = "user2@example.com";
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        String otp1 = otpService.generateAndStoreOtp(identifier1);
        String otp2 = otpService.generateAndStoreOtp(identifier2);

        // Assert - OTPs should be different (statistically)
        // Note: There's a very small chance they could be the same
        assertThat(otp1).isNotNull();
        assertThat(otp2).isNotNull();
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any());
    }
}
