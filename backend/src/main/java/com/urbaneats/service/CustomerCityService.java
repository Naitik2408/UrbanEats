package com.urbaneats.service;

import com.urbaneats.dto.CityCustomerResponse;
import com.urbaneats.entity.City;
import com.urbaneats.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for customer-facing city operations.
 * Provides read-only access to city data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerCityService {

    private final CityRepository cityRepository;

    /**
     * Get all cities sorted by name.
     * No pagination needed as city list is typically small.
     *
     * @return list of cities sorted alphabetically
     */
    @Transactional(readOnly = true)
    public List<CityCustomerResponse> getAllCities() {
        List<City> cities = cityRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        
        return cities.stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map City entity to lightweight CityCustomerResponse.
     * Excludes timestamps and relationships.
     *
     * @param city city entity
     * @return customer response DTO
     */
    private CityCustomerResponse mapToCustomerResponse(City city) {
        return new CityCustomerResponse(
                city.getId(),
                city.getName()
        );
    }
}
