package com.urbaneats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a City.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest {

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    private String name;
}
