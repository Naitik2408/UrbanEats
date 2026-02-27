package com.urbaneats.controller;

import com.urbaneats.dto.CityResponse;
import com.urbaneats.dto.ItemResponse;
import com.urbaneats.dto.RestaurantResponse;
import com.urbaneats.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for search operations.
 * Provides search endpoints for items, restaurants, and cities.
 */
@RestController
@RequestMapping("/api/customer/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * Search items by keyword.
     * 
     * @param q search keyword (required)
     * @param pageable pagination parameters
     * @return page of matching items
     */
    @GetMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<ItemResponse>> searchItems(
            @RequestParam(required = true) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("Search items - keyword: {}", q);
        
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<ItemResponse> items = searchService.searchItems(q.trim(), pageable);
        
        return ResponseEntity.ok(items);
    }

    /**
     * Search restaurants by keyword.
     * 
     * @param q search keyword (required)
     * @param pageable pagination parameters
     * @return page of matching restaurants
     */
    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = true) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("Search restaurants - keyword: {}", q);
        
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<RestaurantResponse> restaurants = searchService.searchRestaurants(q.trim(), pageable);
        
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Search cities by keyword.
     * 
     * @param q search keyword (required)
     * @param pageable pagination parameters
     * @return page of matching cities
     */
    @GetMapping("/cities")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<CityResponse>> searchCities(
            @RequestParam(required = true) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("Search cities - keyword: {}", q);
        
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<CityResponse> cities = searchService.searchCities(q.trim(), pageable);
        
        return ResponseEntity.ok(cities);
    }
}
