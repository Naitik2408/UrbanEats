package com.urbaneats.repository;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests using @DataJpaTest.
 * Tests database interactions, queries, cascade operations, and constraints.
 */
@DataJpaTest(
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
    },
    excludeAutoConfiguration = {
        RedisAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ComponentScan(
    basePackages = "com.urbaneats.repository",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.urbaneats\\.UrbanEatsApplication"
    )
)
@DisplayName("Repository Tests")
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemVariantRepository itemVariantRepository;

    @Autowired
    private ItemAddonRepository itemAddonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ========== City Repository Tests ==========

    @Test
    @DisplayName("Should save and find city by ID")
    void shouldSaveAndFindCityById() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null); // Let DB generate ID
        
        // Act
        City saved = cityRepository.save(city);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context
        
        Optional<City> found = cityRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mumbai");
    }

    @Test
    @DisplayName("Should find all cities")
    void shouldFindAllCities() {
        // Arrange
        City city1 = TestDataBuilder.buildCity();
        city1.setId(null);
        City city2 = TestDataBuilder.buildCity();
        city2.setId(null);
        city2.setName("Delhi");
        
        cityRepository.save(city1);
        cityRepository.save(city2);
        entityManager.flush();

        // Act
        List<City> cities = cityRepository.findAll();

        // Assert
        assertThat(cities).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should enforce unique city name constraint")
    void shouldEnforceUniqueCityNameConstraint() {
        // Arrange
        City city1 = TestDataBuilder.buildCity();
        city1.setId(null);
        City city2 = TestDataBuilder.buildCity();
        city2.setId(null);
        
        cityRepository.save(city1);
        entityManager.flush();

        // Act & Assert
        assertThatThrownBy(() -> {
            cityRepository.save(city2);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // Constraint violation
    }

    // ========== Restaurant Repository Tests ==========

    @Test
    @DisplayName("Should save and find restaurant with city")
    void shouldSaveAndFindRestaurantWithCity() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        
        // Act
        Restaurant saved = restaurantRepository.save(restaurant);
        entityManager.flush();
        entityManager.clear();
        
        Optional<Restaurant> found = restaurantRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("The Pizza Place");
        assertThat(found.get().getCity().getId()).isEqualTo(city.getId());
    }

    @Test
    @DisplayName("Should find restaurants by city ID with pagination")
    void shouldFindRestaurantsByCityIdWithPagination() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant r1 = TestDataBuilder.buildRestaurant(city);
        r1.setId(null);
        Restaurant r2 = TestDataBuilder.buildRestaurant(city);
        r2.setId(null);
        r2.setName("Burger Joint");
        
        restaurantRepository.save(r1);
        restaurantRepository.save(r2);
        entityManager.flush();

        // Act
        Page<Restaurant> page = restaurantRepository.findByCityId(
                city.getId(), PageRequest.of(0, 10));
        final Long cityId = city.getId();

        // Assert
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).allMatch(r -> r.getCity().getId().equals(cityId));
    }

    @Test
    @DisplayName("Should delete restaurant when city is deleted (cascade)")
    void shouldDeleteRestaurantWhenCityIsDeleted() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = restaurantRepository.save(restaurant);
        entityManager.flush();
        
        Long restaurantId = restaurant.getId();

        // Act
        cityRepository.deleteById(city.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Restaurant> found = restaurantRepository.findById(restaurantId);
        assertThat(found).isEmpty();
    }

    // ========== Item Repository Tests ==========

    @Test
    @DisplayName("Should save and find item with restaurant")
    void shouldSaveAndFindItemWithRestaurant() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item = TestDataBuilder.buildSimpleItem(restaurant);
        item.setId(null);
        
        // Act
        Item saved = itemRepository.save(item);
        entityManager.flush();
        entityManager.clear();
        
        Optional<Item> found = itemRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Pepperoni Pizza");
        assertThat(found.get().getRestaurant().getId()).isEqualTo(restaurant.getId());
    }

    @Test
    @DisplayName("Should find items by restaurant ID")
    void shouldFindItemsByRestaurantId() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item1 = TestDataBuilder.buildSimpleItem(restaurant);
        item1.setId(null);
        Item item2 = TestDataBuilder.buildSimpleItem(restaurant);
        item2.setId(null);
        item2.setName("Margherita Pizza");
        
        itemRepository.save(item1);
        itemRepository.save(item2);
        entityManager.flush();

        // Act
        org.springframework.data.domain.Page<Item> itemsPage = itemRepository.findByRestaurantId(restaurant.getId(), org.springframework.data.domain.Pageable.unpaged());
        final Long restaurantId = restaurant.getId();

        // Assert
        assertThat(itemsPage.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(itemsPage.getContent()).allMatch(i -> i.getRestaurant().getId().equals(restaurantId));
    }

    @Test
    @DisplayName("Should delete item when restaurant is deleted (cascade)")
    void shouldDeleteItemWhenRestaurantIsDeleted() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item = TestDataBuilder.buildSimpleItem(restaurant);
        item.setId(null);
        item = itemRepository.save(item);
        entityManager.flush();
        
        Long itemId = item.getId();

        // Act
        restaurantRepository.deleteById(restaurant.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Item> found = itemRepository.findById(itemId);
        assertThat(found).isEmpty();
    }

    // ========== Item Variant Repository Tests ==========

    @Test
    @DisplayName("Should save and find item variants")
    void shouldSaveAndFindItemVariants() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item = TestDataBuilder.buildItemWithVariants(restaurant);
        item.setId(null);
        item = entityManager.persist(item);
        
        ItemVariant variant = TestDataBuilder.buildVariant(item, "Large", new BigDecimal("15.00"));
        variant.setId(null);
        
        // Act
        ItemVariant saved = itemVariantRepository.save(variant);
        entityManager.flush();
        entityManager.clear();
        
        Optional<ItemVariant> found = itemVariantRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Large");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    // Note: ItemVariantRepository doesn't have findByItemId method
    // Variants are typically fetched with the item entity using @OneToMany relationship

    // ========== Item Addon Repository Tests ==========

    @Test
    @DisplayName("Should save and find item addons")
    void shouldSaveAndFindItemAddons() {
        // Arrange
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item = TestDataBuilder.buildItemWithAddons(restaurant);
        item.setId(null);
        item = entityManager.persist(item);
        
        ItemAddon addon = TestDataBuilder.buildAddon(item, "Extra Cheese", new BigDecimal("1.50"));
        addon.setId(null);
        
        // Act
        ItemAddon saved = itemAddonRepository.save(addon);
        entityManager.flush();
        entityManager.clear();
        
        Optional<ItemAddon> found = itemAddonRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Extra Cheese");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("1.50"));
    }

    // ========== User Repository Tests ==========

    @Test
    @DisplayName("Should save and find user by email")
    void shouldSaveAndFindUserByEmail() {
        // Arrange
        User user = TestDataBuilder.buildCustomerUser();
        user.setId(null);
        
        // Act
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        Optional<User> found = userRepository.findByEmail("customer@test.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("customer@test.com");
        assertThat(found.get().getRole()).isEqualTo(User.Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("Should find user by phone")
    void shouldFindUserByPhone() {
        // Arrange
        User user = TestDataBuilder.buildCustomerUser();
        user.setId(null);
        user.setPhone("+919876543210");
        
        userRepository.save(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByPhone("+919876543210");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("+919876543210");
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void shouldEnforceUniqueEmailConstraint() {
        // Arrange
        User user1 = TestDataBuilder.buildCustomerUser();
        user1.setId(null);
        User user2 = TestDataBuilder.buildCustomerUser();
        user2.setId(null);
        
        userRepository.save(user1);
        entityManager.flush();

        // Act & Assert
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    // ========== Admin Repository Tests ==========

    @Test
    @DisplayName("Should save and find admin by username")
    void shouldSaveAndFindAdminByUsername() {
        // Arrange
        Admin admin = TestDataBuilder.buildAdmin();
        admin.setId(null);
        
        // Act
        adminRepository.save(admin);
        entityManager.flush();
        entityManager.clear();
        
        Optional<Admin> found = adminRepository.findByUsername("admin");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin");
        assertThat(found.get().getRole()).isEqualTo(Admin.Role.ROLE_ADMIN);
    }

    // ========== Cart Repository Tests ==========

    @Test
    @DisplayName("Should find cart by user ID with items using JOIN FETCH")
    void shouldFindCartByUserIdWithItems() {
        // Arrange
        User user = TestDataBuilder.buildCustomerUser();
        user.setId(null);
        user = entityManager.persist(user);
        
        Cart cart = TestDataBuilder.buildCart(user);
        cart.setId(null);
        cart = entityManager.persist(cart);
        
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Item item = TestDataBuilder.buildSimpleItem(restaurant);
        item.setId(null);
        item = entityManager.persist(item);
        
        CartItem cartItem = TestDataBuilder.buildCartItem(cart, item, 2, new BigDecimal("16.00"));
        cartItem.setId(null);
        entityManager.persist(cartItem);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Cart> found = cartRepository.findByUserIdWithItems(user.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).isNotEmpty();
        assertThat(found.get().getItems()).hasSize(1);
    }

    // ========== Order Repository Tests ==========

    @Test
    @DisplayName("Should find orders by user ID ordered by created date")
    void shouldFindOrdersByUserIdOrderedByCreatedDate() {
        // Arrange
        User user = TestDataBuilder.buildCustomerUser();
        user.setId(null);
        user = entityManager.persist(user);
        
        City city = TestDataBuilder.buildCity();
        city.setId(null);
        city = entityManager.persist(city);
        
        Restaurant restaurant = TestDataBuilder.buildRestaurant(city);
        restaurant.setId(null);
        restaurant = entityManager.persist(restaurant);
        
        Order order1 = TestDataBuilder.buildOrder(user, new java.math.BigDecimal("28.00"));
        order1.setId(null);
        Order order2 = TestDataBuilder.buildOrder(user, new java.math.BigDecimal("35.00"));
        order2.setId(null);
        
        orderRepository.save(order1);
        orderRepository.save(order2);
        entityManager.flush();

        // Act
        Page<Order> ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));
        final Long userId = user.getId();

        // Assert
        assertThat(ordersPage.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ordersPage.getContent()).allMatch(o -> o.getUser().getId().equals(userId));
    }
}
