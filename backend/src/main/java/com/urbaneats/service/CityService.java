package com.urbaneats.service;

import com.urbaneats.dto.CityRequest;
import com.urbaneats.dto.CityResponse;
import com.urbaneats.entity.City;
import com.urbaneats.repository.CityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing cities.
 * Handles business logic and entity-DTO conversion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CityService {

    private final CityRepository cityRepository;

    /**
     * Create a new city.
     *
     * @param request city request DTO
     * @return created city response
     * @throws DataIntegrityViolationException if city name already exists
     */
    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse createCity(CityRequest request) {
        // Check if city name already exists
        if (cityRepository.existsByName(request.getName())) {
            throw new DataIntegrityViolationException("City with name '" + request.getName() + "' already exists");
        }

        City city = new City();
        city.setName(request.getName());

        City savedCity = cityRepository.save(city);
        
        if (log.isInfoEnabled()) {
            log.info("City created: {} (ID: {})", savedCity.getName(), savedCity.getId());
        }

        return mapToResponse(savedCity);
    }

    /**
     * Update an existing city.
     *
     * @param id city ID
     * @param request city request DTO
     * @return updated city response
     * @throws EntityNotFoundException if city not found
     * @throws DataIntegrityViolationException if new name already exists
     */
    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found with ID: " + id));

        // Check if new name conflicts with existing city
        if (!city.getName().equals(request.getName()) && cityRepository.existsByName(request.getName())) {
            throw new DataIntegrityViolationException("City with name '" + request.getName() + "' already exists");
        }

        city.setName(request.getName());
        City updatedCity = cityRepository.save(city);
        
        if (log.isInfoEnabled()) {
            log.info("City updated: {} (ID: {})", updatedCity.getName(), updatedCity.getId());
        }

        return mapToResponse(updatedCity);
    }

    /**
     * Delete a city.
     * Cascades to delete all restaurants in the city.
     *
     * @param id city ID
     * @throws EntityNotFoundException if city not found
     */
    @Transactional
    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found with ID: " + id));

        cityRepository.delete(city);
        
        if (log.isInfoEnabled()) {
            log.info("City deleted: {} (ID: {})", city.getName(), id);
        }
    }

    /**
     * Get all cities with pagination.
     *
     * @param pageable pagination parameters
     * @return page of city responses
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "cities")
    public Page<CityResponse> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get city by ID.
     *
     * @param id city ID
     * @return city response
     * @throws EntityNotFoundException if city not found
     */
    @Transactional(readOnly = true)
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found with ID: " + id));
        return mapToResponse(city);
    }

    /**
     * Map City entity to CityResponse DTO.
     *
     * @param city city entity
     * @return city response DTO
     */
    private CityResponse mapToResponse(City city) {
        CityResponse response = new CityResponse();
        response.setId(city.getId());
        response.setName(city.getName());
        response.setCreatedAt(city.getCreatedAt());
        response.setUpdatedAt(city.getUpdatedAt());
        return response;
    }
}
