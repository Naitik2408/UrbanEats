package com.urbaneats.controller;

import com.urbaneats.dto.CityRequest;
import com.urbaneats.dto.CityResponse;
import com.urbaneats.service.CityService;
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
 * Admin controller for managing cities.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/cities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCityController {

    private final CityService cityService;

    /**
     * Create a new city.
     * POST /api/admin/cities
     *
     * @param request city request DTO
     * @return created city response
     */
    @PostMapping
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing city.
     * PUT /api/admin/cities/{id}
     *
     * @param id city ID
     * @param request city request DTO
     * @return updated city response
     */
    @PutMapping("/{id}")
    public ResponseEntity<CityResponse> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.updateCity(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a city.
     * DELETE /api/admin/cities/{id}
     *
     * @param id city ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all cities with pagination.
     * GET /api/admin/cities
     *
     * @param pageable pagination parameters (default: page=0, size=20, sort by name)
     * @return page of city responses
     */
    @GetMapping
    public ResponseEntity<Page<CityResponse>> getAllCities(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CityResponse> cities = cityService.getAllCities(pageable);
        return ResponseEntity.ok(cities);
    }

    /**
     * Get city by ID.
     * GET /api/admin/cities/{id}
     *
     * @param id city ID
     * @return city response
     */
    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getCityById(@PathVariable Long id) {
        CityResponse response = cityService.getCityById(id);
        return ResponseEntity.ok(response);
    }
}
