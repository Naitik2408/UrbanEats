package com.urbaneats.repository;

import com.urbaneats.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for City entity.
 * Provides CRUD operations and custom queries for cities.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Find city by name.
     * Used for uniqueness validation.
     *
     * @param name the city name
     * @return optional city
     */
    Optional<City> findByName(String name);

    /**
     * Check if city exists by name.
     *
     * @param name the city name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Search cities by name (case-insensitive, partial match).
     * 
     * @param keyword the search keyword
     * @param pageable pagination parameters
     * @return page of matching cities
     */
    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<City> searchCities(@Param("keyword") String keyword, Pageable pageable);
}
