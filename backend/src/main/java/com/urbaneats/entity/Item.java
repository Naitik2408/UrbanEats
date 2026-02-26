package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a menu Item.
 * Items belong to a restaurant and can have variants and/or addons.
 * 
 * Item Types:
 * 1. No variant, no addon (simple item)
 * 2. Addon only (e.g., Burger with extra toppings)
 * 3. Variant only (e.g., Pizza in Small/Medium/Large)
 * 4. Addon + variant (e.g., Pizza sizes with extra toppings)
 */
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_restaurant", columnList = "restaurant_id"),
    @Index(name = "idx_item_variants", columnList = "has_variants"),
    @Index(name = "idx_item_addons", columnList = "has_addons")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Base price for the item.
     * If item has variants, this may represent the minimum price.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * Whether this item has size/type variants.
     * If true, at least one variant must exist.
     */
    @Column(name = "has_variants", nullable = false)
    private Boolean hasVariants = false;

    /**
     * Whether this item supports addons.
     * If true, at least one addon must exist.
     */
    @Column(name = "has_addons", nullable = false)
    private Boolean hasAddons = false;

    /**
     * Restaurant that serves this item.
     * LAZY fetching by default.
     * Cannot be null - every item must belong to a restaurant.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_restaurant"))
    private Restaurant restaurant;

    /**
     * Variants for this item (e.g., Small, Medium, Large).
     * LAZY fetching to avoid loading all variants.
     * Cascade ALL to manage variant lifecycle with item.
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemVariant> variants = new ArrayList<>();

    /**
     * Addons available for this item (e.g., Extra Cheese).
     * LAZY fetching to avoid loading all addons.
     * Cascade ALL to manage addon lifecycle with item.
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemAddon> addons = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add variant and maintain bidirectional relationship.
     */
    public void addVariant(ItemVariant variant) {
        variants.add(variant);
        variant.setItem(this);
    }

    /**
     * Helper method to add addon and maintain bidirectional relationship.
     */
    public void addAddon(ItemAddon addon) {
        addons.add(addon);
        addon.setItem(this);
    }
}
