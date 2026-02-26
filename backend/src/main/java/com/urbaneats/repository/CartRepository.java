package com.urbaneats.repository;

import com.urbaneats.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Cart entity.
 * Provides data access methods for cart operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID.
     * Uses eager fetching for items to avoid N+1 queries.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Find cart by user ID (without items).
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if cart exists for user.
     */
    boolean existsByUserId(Long userId);
}
