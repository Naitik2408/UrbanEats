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
 * Entity representing a City.
 * Cities contain multiple restaurants.
 */
@Entity
@Table(name = "cities", indexes = {
    @Index(name = "idx_city_name", columnList = "name", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Restaurants in this city.
     * LAZY fetching to avoid loading all restaurants when querying cities.
     * Cascade REMOVE to delete restaurants when city is deleted.
     * orphanRemoval ensures restaurant is deleted if removed from list.
     */
    @OneToMany(mappedBy = "city", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Restaurant> restaurants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
