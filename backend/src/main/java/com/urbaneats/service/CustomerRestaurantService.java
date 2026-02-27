package com.urbaneats.service;

import com.urbaneats.dto.RestaurantCustomerResponse;
import com.urbaneats.dto.RestaurantDetailResponse;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.CityRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for customer-facing restaurant operations.
 * Provides read-only access to restaurant data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CityRepository cityRepository;

    /**
     * Get restaurants by city with pagination.
     * Uses simple query without JOIN FETCH as we don't need related entities.
     *
     * @param cityId city ID
     * @param pageable pagination parameters
     * @return page of restaurants
     * @throws EntityNotFoundException if city not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "restaurants", key = "#cityId")
    public Page<RestaurantCustomerResponse> getRestaurantsByCity(Long cityId, Pageable pageable) {
        // Validate city exists
        if (!cityRepository.existsById(cityId)) {
            throw new EntityNotFoundException("City not found with ID: " + cityId);
        }

        return restaurantRepository.findByCityId(cityId, pageable)
                .map(this::mapToCustomerResponse);
    }

    /**
     * Get restaurant details by ID.
     * Loads city for displaying city name.
     * LAZY loading is acceptable here as it's a single detail query.
     *
     * @param restaurantId restaurant ID
     * @return restaurant details
     * @throws EntityNotFoundException if restaurant not found
     */
    @Transactional(readOnly = true)
    public RestaurantDetailResponse getRestaurantDetails(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + restaurantId));

        return mapToDetailResponse(restaurant);
    }

    /**
     * Map Restaurant entity to lightweight RestaurantCustomerResponse.
     * Used for list view - excludes city details and timestamps.
     *
     * @param restaurant restaurant entity
     * @return customer response DTO
     */
    private RestaurantCustomerResponse mapToCustomerResponse(Restaurant restaurant) {
        return new RestaurantCustomerResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getLandmark(),
                restaurant.getRating()
        );
    }

    /**
     * Map Restaurant entity to RestaurantDetailResponse.
     * Used for detail view - includes city name for context.
     *
     * @param restaurant restaurant entity
     * @return detail response DTO
     */
    private RestaurantDetailResponse mapToDetailResponse(Restaurant restaurant) {
        return new RestaurantDetailResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getLandmark(),
                restaurant.getRating(),
                restaurant.getCity().getName()
        );
    }
}
