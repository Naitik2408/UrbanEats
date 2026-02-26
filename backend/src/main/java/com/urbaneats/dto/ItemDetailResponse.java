package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed response DTO for Item (customer-facing).
 * Includes variants and addons for detail view.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailResponse {

    private Long id;
    private String name;
    private BigDecimal basePrice;
    private Boolean hasVariants;
    private Boolean hasAddons;
    private List<VariantDetail> variants = new ArrayList<>();
    private List<AddonDetail> addons = new ArrayList<>();

    /**
     * Nested DTO for Variant in customer detail view.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDetail {
        private Long id;
        private String name;
        private BigDecimal price;
    }

    /**
     * Nested DTO for Addon in customer detail view.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonDetail {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
