package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItem entity representing an item in an order.
 * 
 * CRITICAL SNAPSHOT DESIGN:
 * All item, variant, and pricing data are stored as snapshots.
 * This prevents issues when menu items are modified or deleted after order placement.
 * 
 * Never query Item/ItemVariant entities for order display.
 * All data needed for order history is stored here.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Order this item belongs to.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;

    /**
     * SNAPSHOT: Item name at the time of order.
     * Stored to display in order history even if item is deleted/renamed.
     */
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    /**
     * SNAPSHOT: Restaurant name at the time of order.
     * Stored to display in order history even if restaurant is deleted/renamed.
     */
    @Column(name = "restaurant_name", nullable = false, length = 200)
    private String restaurantName;

    /**
     * SNAPSHOT: Base price of item at the time of order.
     * If variant was selected, this will be 0 (variant price is used instead).
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * SNAPSHOT: Variant name at the time of order (e.g., "Large").
     * Nullable - only set if variant was selected.
     */
    @Column(name = "variant_name", length = 100)
    private String variantName;

    /**
     * SNAPSHOT: Variant price at the time of order.
     * Nullable - only set if variant was selected.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(name = "variant_price", precision = 10, scale = 2)
    private BigDecimal variantPrice;

    /**
     * Quantity ordered.
     * Must be positive.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Subtotal for this order item.
     * Calculated as: (basePrice OR variantPrice + addonsPrice) * quantity
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Addons with snapshot data.
     * Cascade ALL to manage addon lifecycle.
     * orphanRemoval ensures deleted addons are removed from database.
     */
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemAddon> addons = new ArrayList<>();

    /**
     * Helper method to add an addon to this order item.
     */
    public void addAddon(OrderItemAddon addon) {
        addons.add(addon);
        addon.setOrderItem(this);
    }
}
