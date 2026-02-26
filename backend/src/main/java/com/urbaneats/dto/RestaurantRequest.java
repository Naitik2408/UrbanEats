package com.urbaneats.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a Restaurant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 200, message = "Restaurant name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String address;

    @Size(max = 200, message = "Landmark must not exceed 200 characters")
    private String landmark;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private Double rating;

    @NotNull(message = "City ID is required")
    @Positive(message = "City ID must be positive")
    private Long cityId;
}
