package com.urbaneats.controller;

import com.urbaneats.dto.RestaurantCustomerResponse;
import com.urbaneats.dto.RestaurantDetailResponse;
import com.urbaneats.service.CustomerRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Customer controller for browsing restaurants.
 * Provides read-only access to restaurant data.
 * Public access - no authentication required.
 */
@RestController
@RequestMapping("/api/customer/restaurants")
@RequiredArgsConstructor
public class CustomerRestaurantController {

    private final CustomerRestaurantService customerRestaurantService;

    /**
     * Get restaurants by city with pagination.
     * GET /api/customer/restaurants?cityId={cityId}
     *
     * Query Parameters:
     * - cityId (required): City ID to filter restaurants
     * - page (optional): Page number (default: 0)
     * - size (optional): Page size (default: 20)
     * - sort (optional): Sort field and direction (default: name,ASC)
     *
     * @param cityId city ID
     * @param pageable pagination parameters
     * @return page of restaurants
     */
    @GetMapping
    public ResponseEntity<Page<RestaurantCustomerResponse>> getRestaurantsByCity(
            @RequestParam Long cityId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<RestaurantCustomerResponse> restaurants = customerRestaurantService.getRestaurantsByCity(cityId, pageable);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Get restaurant details by ID.
     * GET /api/customer/restaurants/{id}
     *
     * @param id restaurant ID
     * @return restaurant details
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetails(@PathVariable Long id) {
        RestaurantDetailResponse restaurant = customerRestaurantService.getRestaurantDetails(id);
        return ResponseEntity.ok(restaurant);
    }
}
