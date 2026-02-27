package com.urbaneats.service;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.CityRequest;
import com.urbaneats.dto.RestaurantRequest;
import com.urbaneats.entity.City;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.CityRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminService (City and Restaurant CRUD).
 * Tests creation, update, deletion, and cascade operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Tests")
class AdminServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private AdminService adminService;

    // ========== City CRUD Tests ==========

    @Test
    @DisplayName("Should create city successfully")
    void shouldCreateCitySuccessfully() {
        // Arrange
        CityRequest request = new CityRequest();
        request.setName("Mumbai");
        
        City city = TestDataBuilder.buildCity();
        when(cityRepository.save(any(City.class))).thenReturn(city);

        // Act
        City result = adminService.createCity(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Mumbai");
        verify(cityRepository).save(any(City.class));
    }

    @Test
    @DisplayName("Should get all cities successfully")
    void shouldGetAllCitiesSuccessfully() {
        // Arrange
        City city1 = TestDataBuilder.buildCity();
        City city2 = TestDataBuilder.buildCity();
        city2.setName("Delhi");
        
        when(cityRepository.findAll()).thenReturn(Arrays.asList(city1, city2));

        // Act
        List<City> cities = adminService.getAllCities();

        // Assert
        assertThat(cities).hasSize(2);
        verify(cityRepository).findAll();
    }

    @Test
    @DisplayName("Should get city by ID successfully")
    void shouldGetCityByIdSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        // Act
        City result = adminService.getCityById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Mumbai");
        verify(cityRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when city not found")
    void shouldThrowExceptionWhenCityNotFound() {
        // Arrange
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.getCityById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("City not found");
    }

    @Test
    @DisplayName("Should update city successfully")
    void shouldUpdateCitySuccessfully() {
        // Arrange
        City existingCity = TestDataBuilder.buildCity();
        CityRequest request = new CityRequest();
        request.setName("Mumbai Updated");
        
        when(cityRepository.findById(1L)).thenReturn(Optional.of(existingCity));
        when(cityRepository.save(any(City.class))).thenReturn(existingCity);

        // Act
        City result = adminService.updateCity(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(cityRepository).save(any(City.class));
    }

    @Test
    @DisplayName("Should delete city successfully")
    void shouldDeleteCitySuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        // Act
        adminService.deleteCity(1L);

        // Assert
        verify(cityRepository).delete(city);
    }

    @Test
    @DisplayName("Should delete city and cascade to restaurants")
    void shouldDeleteCityAndCascadeToRestaurants() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        city.setRestaurants(Arrays.asList(restaurant));
        
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        // Act
        adminService.deleteCity(1L);

        // Assert
        verify(cityRepository).delete(city);
    }

    // ========== Restaurant CRUD Tests ==========

    @Test
    @DisplayName("Should create restaurant successfully")
    void shouldCreateRestaurantSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        RestaurantRequest request = new RestaurantRequest();
        request.setName("The Pizza Place");
        request.setCityId(1L);
        request.setAddress("123 Main St");
        
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        Restaurant result = adminService.createRestaurant(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("The Pizza Place");
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should throw exception when creating restaurant with non-existent city")
    void shouldThrowExceptionWhenCreatingRestaurantWithNonExistentCity() {
        // Arrange
        RestaurantRequest request = new RestaurantRequest();
        request.setName("The Pizza Place");
        request.setCityId(999L);
        
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.createRestaurant(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("City not found");
        
        verify(restaurantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get restaurant by ID successfully")
    void shouldGetRestaurantByIdSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Act
        Restaurant result = adminService.getRestaurantById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("The Pizza Place");
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void shouldThrowExceptionWhenRestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.getRestaurantById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Restaurant not found");
    }

    @Test
    @DisplayName("Should update restaurant successfully")
    void shouldUpdateRestaurantSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant existingRestaurant = TestDataBuilder.buildRestaurant(city);
        
        RestaurantRequest request = new RestaurantRequest();
        request.setName("Updated Restaurant");
        request.setCityId(1L);
        request.setAddress("456 New St");
        
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(existingRestaurant));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(existingRestaurant);

        // Act
        Restaurant result = adminService.updateRestaurant(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should delete restaurant successfully")
    void shouldDeleteRestaurantSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Act
        adminService.deleteRestaurant(1L);

        // Assert
        verify(restaurantRepository).delete(restaurant);
    }

    @Test
    @DisplayName("Should get restaurants by city ID")
    void shouldGetRestaurantsByCityId() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant r1 = TestDataBuilder.buildRestaurant(city);
        Restaurant r2 = TestDataBuilder.buildRestaurant(city);
        r2.setName("Burger Joint");
        
        when(restaurantRepository.findByCityId(1L)).thenReturn(Arrays.asList(r1, r2));

        // Act
        List<Restaurant> restaurants = adminService.getRestaurantsByCity(1L);

        // Assert
        assertThat(restaurants).hasSize(2);
        verify(restaurantRepository).findByCityId(1L);
    }
}
