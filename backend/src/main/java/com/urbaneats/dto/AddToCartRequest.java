package com.urbaneats.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for adding an item to cart.
 * Frontend sends item, variant, addons, and quantity.
 * Price is calculated server-side - never trust frontend input.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    /**
     * Variant ID (nullable).
     * Required if item has variants.
     */
    private Long variantId;

    /**
     * List of addon IDs.
     * Can be empty if item has no addons.
     */
    private List<Long> addonIds;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
