package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP response.
 * For development, includes the OTP. In production, this should not be sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private String message;
    private String otp;  // Only for development

    public OtpResponse(String message) {
        this.message = message;
    }
}
