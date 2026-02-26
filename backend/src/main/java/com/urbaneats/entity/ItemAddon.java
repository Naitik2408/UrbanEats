package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing an addon for an Item.
 * Examples: Extra Cheese, Extra Sauce, Extra Toppings.
 * Each addon has an additional price.
 */
@Entity
@Table(name = "item_addons", indexes = {
    @Index(name = "idx_addon_item", columnList = "item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Additional price for this addon.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Item this addon belongs to.
     * LAZY fetching by default.
     * Cannot be null - every addon must belong to an item.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_addon_item"))
    private Item item;
}
