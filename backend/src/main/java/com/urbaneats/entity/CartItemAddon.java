package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Junction entity linking CartItem with ItemAddon.
 * Represents addons selected for a specific cart item.
 */
@Entity
@Table(name = "cart_item_addons", indexes = {
    @Index(name = "idx_cart_item_addon_cart_item", columnList = "cart_item_id"),
    @Index(name = "idx_cart_item_addon_addon", columnList = "addon_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cart item this addon belongs to.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_item_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_cart_item_addon_cart_item"))
    private CartItem cartItem;

    /**
     * Addon selected for this cart item.
     * LAZY fetching by default.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_cart_item_addon_addon"))
    private ItemAddon addon;
}
