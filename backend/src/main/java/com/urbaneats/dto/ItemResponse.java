package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for Item.
 * Includes variants and addons if applicable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String name;
    private BigDecimal basePrice;
    private Long restaurantId;
    private String restaurantName;
    private Boolean hasVariants;
    private Boolean hasAddons;
    private List<VariantResponse> variants = new ArrayList<>();
    private List<AddonResponse> addons = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested DTO for Variant in response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantResponse {
        private Long id;
        private String name;
        private BigDecimal price;
    }

    /**
     * Nested DTO for Addon in response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonResponse {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
