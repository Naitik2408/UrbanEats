package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a placed order.
 * Orders capture snapshot data from cart to prevent issues when menu prices change.
 * 
 * CRITICAL DESIGN PRINCIPLE:
 * Never rely on Item/Variant/Addon entities after order placement.
 * All names and prices are stored as snapshots in OrderItem and OrderItemAddon.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who placed this order.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_user"))
    private User user;

    /**
     * Total amount for this order.
     * Calculated from all order items.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Order status.
     * PLACED: Order successfully placed.
     * CANCELLED: Order cancelled within 60-second window.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    /**
     * Order items with snapshot data.
     * Cascade ALL to manage order item lifecycle.
     * orphanRemoval ensures deleted items are removed from database.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Helper method to add an item to the order.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Check if order can be cancelled (within 60-second window).
     */
    public boolean canBeCancelled() {
        if (status == OrderStatus.CANCELLED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = createdAt.plusSeconds(60);
        return now.isBefore(cutoff);
    }

    public enum OrderStatus {
        PLACED,
        CANCELLED
    }
}
