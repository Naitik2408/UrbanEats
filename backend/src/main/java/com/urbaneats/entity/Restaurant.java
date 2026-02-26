package com.urbaneats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Restaurant.
 * Restaurants belong to a city and contain multiple items.
 */
@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurant_city", columnList = "city_id"),
    @Index(name = "idx_restaurant_rating", columnList = "rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 200)
    private String landmark;

    /**
     * Rating out of 5.0.
     * Default 0.0 for new restaurants.
     */
    @Column(nullable = false)
    private Double rating = 0.0;

    /**
     * City where this restaurant is located.
     * LAZY fetching by default.
     * Cannot be null - every restaurant must belong to a city.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_restaurant_city"))
    private City city;

    /**
     * Items available in this restaurant.
     * LAZY fetching to avoid loading all items.
     * Cascade REMOVE to delete items when restaurant is deleted.
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
