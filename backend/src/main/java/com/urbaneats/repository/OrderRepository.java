package com.urbaneats.repository;

import com.urbaneats.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Order entity.
 * Provides data access methods for order operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user ID with pagination.
     * Orders are sorted by creation date descending (newest first).
     * Uses JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdWithItems(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find order by ID with items eagerly loaded.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Find order by ID and user ID.
     * Used to verify user owns the order before cancellation.
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
