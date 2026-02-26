package com.urbaneats.repository;

import com.urbaneats.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Restaurant entity.
 * Provides CRUD operations and custom queries for restaurants.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Find all restaurants in a specific city.
     * Supports pagination.
     *
     * @param cityId the city ID
     * @param pageable pagination parameters
     * @return page of restaurants
     */
    Page<Restaurant> findByCityId(Long cityId, Pageable pageable);
}
