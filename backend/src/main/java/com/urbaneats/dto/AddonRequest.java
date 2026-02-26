package com.urbaneats.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Item Addon.
 * Used in ItemRequest for creating/updating addons.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddonRequest {

    @NotBlank(message = "Addon name is required")
    @Size(min = 1, max = 100, message = "Addon name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Addon price is required")
    @DecimalMin(value = "0.01", message = "Addon price must be at least 0.01")
    private BigDecimal price;
}
