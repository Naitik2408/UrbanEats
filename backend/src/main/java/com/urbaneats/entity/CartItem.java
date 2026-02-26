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
 * CartItem entity representing an item added to a cart.
 * Includes item, optional variant, quantity, and calculated total price.
 * Can have multiple addons through CartItemAddon junction table.
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_item", columnList = "item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cart this item belongs to.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    /**
     * Menu item being added to cart.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_item"))
    private Item item;

    /**
     * Variant selected for this item (e.g., Small, Medium, Large).
     * Nullable - only required if item.hasVariants is true.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", foreignKey = @ForeignKey(name = "fk_cart_item_variant"))
    private ItemVariant variant;

    /**
     * Quantity of this item in the cart.
     * Must be positive.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Total price for this cart item.
     * Calculated as: (itemPrice + addonsPrice) * quantity
     * Server-side calculation only - never trust frontend input.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Addons selected for this cart item.
     * Cascade ALL to manage addon lifecycle.
     * orphanRemoval ensures deleted addons are removed from database.
     */
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItemAddon> addons = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Helper method to add an addon to this cart item.
     */
    public void addAddon(CartItemAddon addon) {
        addons.add(addon);
        addon.setCartItem(this);
    }

    /**
     * Helper method to remove an addon from this cart item.
     */
    public void removeAddon(CartItemAddon addon) {
        addons.remove(addon);
        addon.setCartItem(null);
    }
}
