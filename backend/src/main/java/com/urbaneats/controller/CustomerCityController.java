package com.urbaneats.controller;

import com.urbaneats.dto.CityCustomerResponse;
import com.urbaneats.service.CustomerCityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer controller for browsing cities.
 * Provides read-only access to city data.
 * Public access - no authentication required.
 */
@RestController
@RequestMapping("/api/customer/cities")
@RequiredArgsConstructor
public class CustomerCityController {

    private final CustomerCityService customerCityService;

    /**
     * Get all cities sorted by name.
     * GET /api/customer/cities
     *
     * No pagination as city list is typically small.
     *
     * @return list of cities
     */
    @GetMapping
    public ResponseEntity<List<CityCustomerResponse>> getAllCities() {
        List<CityCustomerResponse> cities = customerCityService.getAllCities();
        return ResponseEntity.ok(cities);
    }
}
