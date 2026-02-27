package com.urbaneats.service;

import com.urbaneats.dto.RestaurantRequest;
import com.urbaneats.dto.RestaurantResponse;
import com.urbaneats.entity.City;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.CityRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing restaurants.
 * Handles business logic and entity-DTO conversion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CityRepository cityRepository;

    /**
     * Create a new restaurant.
     *
     * @param request restaurant request DTO
     * @return created restaurant response
     * @throws EntityNotFoundException if city not found
     */
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        // Validate city exists
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new EntityNotFoundException("City not found with ID: " + request.getCityId()));

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setLandmark(request.getLandmark());
        restaurant.setRating(request.getRating());
        restaurant.setCity(city);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        if (log.isInfoEnabled()) {
            log.info("Restaurant created: {} in {} (ID: {})", 
                savedRestaurant.getName(), city.getName(), savedRestaurant.getId());
        }

        return mapToResponse(savedRestaurant);
    }

    /**
     * Update an existing restaurant.
     *
     * @param id restaurant ID
     * @param request restaurant request DTO
     * @return updated restaurant response
     * @throws EntityNotFoundException if restaurant or city not found
     */
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));

        // Validate city exists if changed
        if (!restaurant.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found with ID: " + request.getCityId()));
            restaurant.setCity(city);
        }

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setLandmark(request.getLandmark());
        restaurant.setRating(request.getRating());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        
        if (log.isInfoEnabled()) {
            log.info("Restaurant updated: {} (ID: {})", updatedRestaurant.getName(), updatedRestaurant.getId());
        }

        return mapToResponse(updatedRestaurant);
    }

    /**
     * Delete a restaurant.
     * Cascades to delete all items in the restaurant.
     *
     * @param id restaurant ID
     * @throws EntityNotFoundException if restaurant not found
     */
    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));

        restaurantRepository.delete(restaurant);
        
        if (log.isInfoEnabled()) {
            log.info("Restaurant deleted: {} (ID: {})", restaurant.getName(), id);
        }
    }

    /**
     * Get restaurants by city with pagination.
     *
     * @param cityId city ID
     * @param pageable pagination parameters
     * @return page of restaurant responses
     * @throws EntityNotFoundException if city not found
     */
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getRestaurantsByCity(Long cityId, Pageable pageable) {
        // Validate city exists
        if (!cityRepository.existsById(cityId)) {
            throw new EntityNotFoundException("City not found with ID: " + cityId);
        }

        return restaurantRepository.findByCityId(cityId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get all restaurants with pagination.
     *
     * @param pageable pagination parameters
     * @return page of restaurant responses
     */
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get restaurant by ID.
     *
     * @param id restaurant ID
     * @return restaurant response
     * @throws EntityNotFoundException if restaurant not found
     */
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));
        return mapToResponse(restaurant);
    }

    /**
     * Map Restaurant entity to RestaurantResponse DTO.
     *
     * @param restaurant restaurant entity
     * @return restaurant response DTO
     */
    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setLandmark(restaurant.getLandmark());
        response.setRating(restaurant.getRating());
        response.setCityId(restaurant.getCity().getId());
        response.setCityName(restaurant.getCity().getName());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        return response;
    }
}
