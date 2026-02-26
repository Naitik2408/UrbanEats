package com.urbaneats.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP request.
 * Either email or phone must be provided (not both).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    /**
     * Validate that exactly one identifier is provided.
     *
     * @return the identifier (email or phone)
     * @throws IllegalArgumentException if validation fails
     */
    public String getIdentifier() {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException("Either email or phone must be provided");
        }
        if (email != null && !email.isBlank() && phone != null && !phone.isBlank()) {
            throw new IllegalArgumentException("Provide either email or phone, not both");
        }
        return email != null && !email.isBlank() ? email : phone;
    }
}
