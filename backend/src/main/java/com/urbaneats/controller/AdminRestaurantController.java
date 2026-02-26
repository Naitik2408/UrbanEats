package com.urbaneats.controller;

import com.urbaneats.dto.RestaurantRequest;
import com.urbaneats.dto.RestaurantResponse;
import com.urbaneats.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing restaurants.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;

    /**
     * Create a new restaurant.
     * POST /api/admin/restaurants
     *
     * @param request restaurant request DTO
     * @return created restaurant response
     */
    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing restaurant.
     * PUT /api/admin/restaurants/{id}
     *
     * @param id restaurant ID
     * @param request restaurant request DTO
     * @return updated restaurant response
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.updateRestaurant(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a restaurant.
     * DELETE /api/admin/restaurants/{id}
     *
     * @param id restaurant ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all restaurants with pagination.
     * GET /api/admin/restaurants
     *
     * @param pageable pagination parameters (default: page=0, size=20, sort by name)
     * @return page of restaurant responses
     */
    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurants(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<RestaurantResponse> restaurants = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Get restaurants by city with pagination.
     * GET /api/admin/restaurants/city/{cityId}
     *
     * @param cityId city ID
     * @param pageable pagination parameters (default: page=0, size=20, sort by name)
     * @return page of restaurant responses
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<Page<RestaurantResponse>> getRestaurantsByCity(
            @PathVariable Long cityId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<RestaurantResponse> restaurants = restaurantService.getRestaurantsByCity(cityId, pageable);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Get restaurant by ID.
     * GET /api/admin/restaurants/{id}
     *
     * @param id restaurant ID
     * @return restaurant response
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }
}
