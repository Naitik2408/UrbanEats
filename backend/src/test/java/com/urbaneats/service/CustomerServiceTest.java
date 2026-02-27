package com.urbaneats.service;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.entity.City;
import com.urbaneats.entity.Item;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.CityRepository;
import com.urbaneats.repository.ItemRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService (browsing flow).
 * Tests city listing, restaurant browsing, item viewing, and pagination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CustomerService customerService;

    // ========== List Cities Tests ==========

    @Test
    @DisplayName("Should list all cities successfully")
    void shouldListAllCitiesSuccessfully() {
        // Arrange
        City city1 = TestDataBuilder.buildCity();
        City city2 = TestDataBuilder.buildCity();
        city2.setId(2L);
        city2.setName("Delhi");
        
        when(cityRepository.findAll()).thenReturn(Arrays.asList(city1, city2));

        // Act
        List<City> cities = customerService.listAllCities();

        // Assert
        assertThat(cities).hasSize(2);
        assertThat(cities).extracting("name").containsExactlyInAnyOrder("Mumbai", "Delhi");
        verify(cityRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no cities exist")
    void shouldReturnEmptyListWhenNoCitiesExist() {
        // Arrange
        when(cityRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<City> cities = customerService.listAllCities();

        // Assert
        assertThat(cities).isEmpty();
    }

    // ========== List Restaurants by City Tests ==========

    @Test
    @DisplayName("Should list restaurants by city with pagination")
    void shouldListRestaurantsByCityWithPagination() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant r1 = TestDataBuilder.buildRestaurant(city);
        Restaurant r2 = TestDataBuilder.buildRestaurant(city);
        r2.setId(2L);
        r2.setName("Burger Joint");
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = new PageImpl<>(Arrays.asList(r1, r2), pageable, 2);
        
        when(restaurantRepository.findByCityId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Restaurant> result = customerService.listRestaurantsByCity(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(restaurantRepository).findByCityId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no restaurants in city")
    void shouldReturnEmptyPageWhenNoRestaurantsInCity() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(restaurantRepository.findByCityId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Restaurant> result = customerService.listRestaurantsByCity(1L, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle pagination correctly for multiple pages")
    void shouldHandlePaginationCorrectlyForMultiplePages() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant r1 = TestDataBuilder.buildRestaurant(city);
        
        Pageable pageable = PageRequest.of(0, 1);
        Page<Restaurant> page = new PageImpl<>(Arrays.asList(r1), pageable, 5); // Total 5 items
        
        when(restaurantRepository.findByCityId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Restaurant> result = customerService.listRestaurantsByCity(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(5);
    }

    // ========== Get Restaurant Details Tests ==========

    @Test
    @DisplayName("Should get restaurant details successfully")
    void shouldGetRestaurantDetailsSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Act
        Restaurant result = customerService.getRestaurantDetails(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("The Pizza Place");
        assertThat(result.getCity().getName()).isEqualTo("Mumbai");
        verify(restaurantRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void shouldThrowExceptionWhenRestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getRestaurantDetails(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Restaurant not found");
    }

    // ========== List Items by Restaurant Tests ==========

    @Test
    @DisplayName("Should list items by restaurant successfully")
    void shouldListItemsByRestaurantSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        Item item1 = TestDataBuilder.buildSimpleItem(restaurant);
        Item item2 = TestDataBuilder.buildItemWithVariants(restaurant);
        item2.setId(2L);
        item2.setName("Margherita Pizza");
        
        when(itemRepository.findByRestaurantId(1L)).thenReturn(Arrays.asList(item1, item2));

        // Act
        List<Item> items = customerService.listItemsByRestaurant(1L);

        // Assert
        assertThat(items).hasSize(2);
        assertThat(items).allMatch(item -> item.getRestaurant().getId().equals(1L));
        verify(itemRepository).findByRestaurantId(1L);
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no items")
    void shouldReturnEmptyListWhenRestaurantHasNoItems() {
        // Arrange
        when(itemRepository.findByRestaurantId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Item> items = customerService.listItemsByRestaurant(1L);

        // Assert
        assertThat(items).isEmpty();
    }

    // ========== Get Item Details Tests ==========

    @Test
    @DisplayName("Should get item details successfully")
    void shouldGetItemDetailsSuccessfully() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        Item item = TestDataBuilder.buildSimpleItem(restaurant);
        
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        Item result = customerService.getItemDetails(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pepperoni Pizza");
        assertThat(result.getRestaurant().getName()).isEqualTo("The Pizza Place");
        verify(itemRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    void shouldThrowExceptionWhenItemNotFound() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getItemDetails(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    @DisplayName("Should get item with variants and addons")
    void shouldGetItemWithVariantsAndAddons() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        Item item = TestDataBuilder.buildItemWithVariants(restaurant);
        
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        Item result = customerService.getItemDetails(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isHasVariants()).isTrue();
        assertThat(result.isHasAddons()).isFalse();
    }

    // ========== Search Tests ==========

    @Test
    @DisplayName("Should search restaurants by name")
    void shouldSearchRestaurantsByName() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        
        when(restaurantRepository.findByNameContainingIgnoreCase("Pizza")).thenReturn(Arrays.asList(restaurant));

        // Act
        List<Restaurant> results = customerService.searchRestaurants("Pizza");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).contains("Pizza");
        verify(restaurantRepository).findByNameContainingIgnoreCase("Pizza");
    }

    @Test
    @DisplayName("Should return empty list when no restaurants match search")
    void shouldReturnEmptyListWhenNoRestaurantsMatchSearch() {
        // Arrange
        when(restaurantRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(Arrays.asList());

        // Act
        List<Restaurant> results = customerService.searchRestaurants("NonExistent");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should search items by name")
    void shouldSearchItemsByName() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        Item item = TestDataBuilder.buildSimpleItem(restaurant);
        
        when(itemRepository.findByNameContainingIgnoreCase("Pizza")).thenReturn(Arrays.asList(item));

        // Act
        List<Item> results = customerService.searchItems("Pizza");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).contains("Pizza");
        verify(itemRepository).findByNameContainingIgnoreCase("Pizza");
    }
}
