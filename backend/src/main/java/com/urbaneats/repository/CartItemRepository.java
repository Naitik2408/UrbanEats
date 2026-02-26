package com.urbaneats.repository;

import com.urbaneats.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CartItem entity.
 * Provides data access methods for cart item operations.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by ID with addons eagerly loaded.
     * Useful for price recalculation.
     */
    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.addons WHERE ci.id = :id")
    Optional<CartItem> findByIdWithAddons(@Param("id") Long id);
}
