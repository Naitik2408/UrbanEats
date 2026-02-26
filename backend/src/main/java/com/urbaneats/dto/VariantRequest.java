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
 * DTO for Item Variant.
 * Used in ItemRequest for creating/updating variants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequest {

    @NotBlank(message = "Variant name is required")
    @Size(min = 1, max = 100, message = "Variant name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.01", message = "Variant price must be at least 0.01")
    private BigDecimal price;
}
