package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for cart details.
 * Contains list of cart items and total amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private Integer itemCount;

    /**
     * DTO for individual cart item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long itemId;
        private String itemName;
        private BigDecimal basePrice;
        private VariantDto variant;
        private List<AddonDto> addons;
        private Integer quantity;
        private BigDecimal totalPrice;
    }

    /**
     * DTO for variant in cart item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantDto {
        private Long variantId;
        private String name;
        private BigDecimal price;
    }

    /**
     * DTO for addon in cart item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddonDto {
        private Long addonId;
        private String name;
        private BigDecimal price;
    }
}
