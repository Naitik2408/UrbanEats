package com.urbaneats.repository;

import com.urbaneats.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Item entity.
 * Provides CRUD operations and custom queries for items.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items in a specific restaurant.
     * Supports pagination.
     *
     * @param restaurantId the restaurant ID
     * @param pageable pagination parameters
     * @return page of items
     */
    Page<Item> findByRestaurantId(Long restaurantId, Pageable pageable);

    /**
     * Find item by ID with variants loaded.
     * Uses JOIN FETCH to avoid N+1 problem.
     *
     * @param itemId the item ID
     * @return item with variants
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.variants WHERE i.id = :itemId")
    Item findByIdWithVariants(@Param("itemId") Long itemId);

    /**
     * Find item by ID with addons loaded.
     * Uses JOIN FETCH to avoid N+1 problem.
     *
     * @param itemId the item ID
     * @return item with addons
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.addons WHERE i.id = :itemId")
    Item findByIdWithAddons(@Param("itemId") Long itemId);

    /**
     * Find item by ID with both variants and addons loaded.
     * Uses JOIN FETCH to load all related data in a single query.
     * Optimized for customer detail view to avoid N+1 problem.
     *
     * @param itemId the item ID
     * @return item with variants and addons, or null if not found
     */
    @Query("SELECT DISTINCT i FROM Item i " +
           "LEFT JOIN FETCH i.variants " +
           "LEFT JOIN FETCH i.addons " +
           "WHERE i.id = :itemId")
    Item findByIdWithVariantsAndAddons(@Param("itemId") Long itemId);

    /**
     * Search items by name (case-insensitive, partial match).
     * 
     * @param keyword the search keyword
     * @param pageable pagination parameters
     * @return page of matching items
     */
    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Item> searchItems(@Param("keyword") String keyword, Pageable pageable);
}
