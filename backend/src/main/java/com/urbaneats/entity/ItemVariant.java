package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a variant of an Item.
 * Examples: Small, Medium, Large for pizza.
 * Each variant has its own price.
 */
@Entity
@Table(name = "item_variants", indexes = {
    @Index(name = "idx_variant_item", columnList = "item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Price for this variant.
     * Precision: 10 digits total, 2 decimal places.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Item this variant belongs to.
     * LAZY fetching by default.
     * Cannot be null - every variant must belong to an item.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_variant_item"))
    private Item item;
}
