package com.urbaneats.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating or updating an Item.
 * Supports items with variants and/or addons.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 200, message = "Item name must be between 2 and 200 characters")
    private String name;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be at least 0.01")
    private BigDecimal basePrice;

    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;

    @NotNull(message = "hasVariants flag is required")
    private Boolean hasVariants;

    @NotNull(message = "hasAddons flag is required")
    private Boolean hasAddons;

    /**
     * List of variants for this item.
     * Required if hasVariants = true.
     */
    @Valid
    private List<VariantRequest> variants = new ArrayList<>();

    /**
     * List of addons for this item.
     * Required if hasAddons = true.
     */
    @Valid
    private List<AddonRequest> addons = new ArrayList<>();
}
