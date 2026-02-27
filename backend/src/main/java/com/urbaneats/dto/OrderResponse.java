package com.urbaneats.dto;

import com.urbaneats.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for order details.
 * Contains order metadata and items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;
    private String restaurantName;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
    private Boolean canBeCancelled;
    private List<OrderItemDto> items;

    /**
     * DTO for individual order item.
     * Contains snapshot data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long orderItemId;
        private String itemName;
        private BigDecimal basePrice;
        private String variantName;
        private BigDecimal variantPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private List<OrderItemAddonDto> addons;
    }

    /**
     * DTO for addon in order item.
     * Contains snapshot data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemAddonDto {
        private String addonName;
        private BigDecimal addonPrice;
    }
}
