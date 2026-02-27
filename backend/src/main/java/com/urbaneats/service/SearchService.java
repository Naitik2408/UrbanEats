package com.urbaneats.service;

import com.urbaneats.dto.CityResponse;
import com.urbaneats.dto.ItemResponse;
import com.urbaneats.dto.RestaurantResponse;
import com.urbaneats.entity.City;
import com.urbaneats.entity.Item;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.CityRepository;
import com.urbaneats.repository.ItemRepository;
import com.urbaneats.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for search operations.
 * Provides SQL-based search for items, restaurants, and cities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CityRepository cityRepository;

    /**
     * Search items by keyword.
     * Case-insensitive partial match on item name.
     * 
     * @param keyword search keyword
     * @param pageable pagination parameters
     * @return page of matching items
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItems(String keyword, Pageable pageable) {
        log.info("Searching items - keyword: {}", keyword);
        
        Page<Item> items = itemRepository.searchItems(keyword, pageable);
        
        return items.map(this::mapToItemResponse);
    }

    /**
     * Search restaurants by keyword.
     * Case-insensitive partial match on restaurant name.
     * 
     * @param keyword search keyword
     * @param pageable pagination parameters
     * @return page of matching restaurants
     */
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchRestaurants(String keyword, Pageable pageable) {
        log.info("Searching restaurants - keyword: {}", keyword);
        
        Page<Restaurant> restaurants = restaurantRepository.searchRestaurants(keyword, pageable);
        
        return restaurants.map(this::mapToRestaurantResponse);
    }

    /**
     * Search cities by keyword.
     * Case-insensitive partial match on city name.
     * 
     * @param keyword search keyword
     * @param pageable pagination parameters
     * @return page of matching cities
     */
    @Transactional(readOnly = true)
    public Page<CityResponse> searchCities(String keyword, Pageable pageable) {
        log.info("Searching cities - keyword: {}", keyword);
        
        Page<City> cities = cityRepository.searchCities(keyword, pageable);
        
        return cities.map(this::mapToCityResponse);
    }

    /**
     * Map Item entity to ItemResponse DTO.
     */
    private ItemResponse mapToItemResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setBasePrice(item.getBasePrice());
        response.setHasVariants(item.getHasVariants());
        response.setHasAddons(item.getHasAddons());
        response.setRestaurantId(item.getRestaurant().getId());
        response.setRestaurantName(item.getRestaurant().getName());
        return response;
    }

    /**
     * Map Restaurant entity to RestaurantResponse DTO.
     */
    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setLandmark(restaurant.getLandmark());
        response.setRating(restaurant.getRating());
        response.setCityId(restaurant.getCity().getId());
        response.setCityName(restaurant.getCity().getName());
        return response;
    }

    /**
     * Map City entity to CityResponse DTO.
     */
    private CityResponse mapToCityResponse(City city) {
        CityResponse response = new CityResponse();
        response.setId(city.getId());
        response.setName(city.getName());
        return response;
    }
}
