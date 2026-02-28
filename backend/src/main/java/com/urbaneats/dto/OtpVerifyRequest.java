package com.urbaneats.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP verification request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;
    
    /**
     * Optional Firebase ID token for phone authentication.
     * When provided, backend will verify this token instead of OTP.
     */
    private String firebaseToken;
}
