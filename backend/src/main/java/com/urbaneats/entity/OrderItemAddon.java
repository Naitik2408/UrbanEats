package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItemAddon entity representing an addon in an order item.
 * 
 * CRITICAL SNAPSHOT DESIGN:
 * Addon name and price are stored as snapshots.
 * This prevents issues when addons are modified or deleted after order placement.
 * 
 * Never query ItemAddon entity for order display.
 * All data needed for order history is stored here.
 */
@Entity
@Table(name = "order_item_addons", indexes = {
    @Index(name = "idx_order_item_addon_order_item", columnList = "order_item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Order item this addon belongs to.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_order_item_addon_order_item"))
    private OrderItem orderItem;

    /**
     * SNAPSHOT: Addon name at the time of order.
     * Stored to display in order history even if addon is deleted/renamed.
     */
    @Column(name = "addon_name", nullable = false, length = 100)
    private String addonName;

    /**
     * SNAPSHOT: Addon price at the time of order.
     * Stored to display in order history even if price changes.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(name = "addon_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal addonPrice;
}
