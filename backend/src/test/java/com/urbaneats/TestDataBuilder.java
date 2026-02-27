package com.urbaneats;

import com.urbaneats.entity.*;
import com.urbaneats.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder for creating test entities and DTOs.
 * Provides convenient methods to build test objects with sensible defaults.
 */
public class TestDataBuilder {

    // ========== User Builders ==========

    public static User buildCustomerUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("customer@test.com");
        user.setRole(User.Role.ROLE_CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static User buildCustomerUserWithId(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(User.Role.ROLE_CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    // ========== Admin Builders ==========

    public static Admin buildAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("$2a$10$encoded.password.hash");
        admin.setRole(Admin.Role.ROLE_ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }

    // ========== City Builders ==========

    public static City buildCity() {
        City city = new City();
        city.setId(1L);
        city.setName("Mumbai");
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        city.setRestaurants(new ArrayList<>());
        return city;
    }

    public static City buildCityWithName(String name) {
        City city = new City();
        city.setName(name);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        city.setRestaurants(new ArrayList<>());
        return city;
    }

    public static CityResponse buildCityResponse() {
        return new CityResponse(1L, "Mumbai", LocalDateTime.now(), LocalDateTime.now());
    }

    public static CityResponse buildCityResponse(Long id, String name) {
        return new CityResponse(id, name, LocalDateTime.now(), LocalDateTime.now());
    }

    // ========== Restaurant Builders ==========

    public static Restaurant buildRestaurant(City city) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setCity(city);
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurant.setItems(new ArrayList<>());
        return restaurant;
    }

    public static Restaurant buildRestaurantWithName(String name, City city) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCity(city);
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurant.setItems(new ArrayList<>());
        return restaurant;
    }

    // ========== Item Builders ==========

    public static Item buildSimpleItem(Restaurant restaurant) {
        Item item = new Item();
        item.setId(1L);
        item.setName("Simple Burger");
        item.setBasePrice(new BigDecimal("8.00"));
        item.setHasVariants(false);
        item.setHasAddons(false);
        item.setRestaurant(restaurant);
        item.setVariants(new ArrayList<>());
        item.setAddons(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    public static Item buildItemWithVariants(Restaurant restaurant) {
        Item item = new Item();
        item.setId(2L);
        item.setName("Pizza");
        item.setBasePrice(new BigDecimal("10.00"));
        item.setHasVariants(true);
        item.setHasAddons(false);
        item.setRestaurant(restaurant);
        item.setVariants(new ArrayList<>());
        item.setAddons(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    public static Item buildItemWithAddons(Restaurant restaurant) {
        Item item = new Item();
        item.setId(3L);
        item.setName("Burger with Addons");
        item.setBasePrice(new BigDecimal("8.00"));
        item.setHasVariants(false);
        item.setHasAddons(true);
        item.setRestaurant(restaurant);
        item.setVariants(new ArrayList<>());
        item.setAddons(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    public static Item buildItemWithVariantsAndAddons(Restaurant restaurant) {
        Item item = new Item();
        item.setId(4L);
        item.setName("Deluxe Pizza");
        item.setBasePrice(new BigDecimal("10.00"));
        item.setHasVariants(true);
        item.setHasAddons(true);
        item.setRestaurant(restaurant);
        item.setVariants(new ArrayList<>());
        item.setAddons(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    // ========== ItemVariant Builders ==========

    public static ItemVariant buildVariant(Item item, String name, BigDecimal price) {
        ItemVariant variant = new ItemVariant();
        variant.setId(1L);
        variant.setName(name);
        variant.setPrice(price);
        variant.setItem(item);
        return variant;
    }

    // ========== ItemAddon Builders ==========

    public static ItemAddon buildAddon(Item item, String name, BigDecimal price) {
        ItemAddon addon = new ItemAddon();
        addon.setId(1L);
        addon.setName(name);
        addon.setPrice(price);
        addon.setItem(item);
        return addon;
    }

    // ========== Cart Builders ==========

    public static Cart buildCart(User user) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cart;
    }

    public static CartItem buildCartItem(Cart cart, Item item, Integer quantity, BigDecimal totalPrice) {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(totalPrice);
        cartItem.setAddons(new ArrayList<>());
        cartItem.setCreatedAt(LocalDateTime.now());
        return cartItem;
    }

    public static CartItem buildCartItemWithVariant(Cart cart, Item item, ItemVariant variant, Integer quantity, BigDecimal totalPrice) {
        CartItem cartItem = buildCartItem(cart, item, quantity, totalPrice);
        cartItem.setVariant(variant);
        return cartItem;
    }

    // ========== Order Builders ==========

    public static Order buildOrder(User user, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PLACED);
        order.setItems(new ArrayList<>());
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    public static Order buildOrderWithCreatedAt(User user, BigDecimal totalAmount, LocalDateTime createdAt) {
        Order order = buildOrder(user, totalAmount);
        order.setCreatedAt(createdAt);
        return order;
    }

    public static OrderItem buildOrderItem(Order order, String itemName, BigDecimal basePrice, Integer quantity, BigDecimal subtotal) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setItemName(itemName);
        orderItem.setBasePrice(basePrice);
        orderItem.setQuantity(quantity);
        orderItem.setSubtotal(subtotal);
        orderItem.setAddons(new ArrayList<>());
        return orderItem;
    }

    public static OrderItem buildOrderItemWithVariant(Order order, String itemName, String variantName, BigDecimal variantPrice, Integer quantity, BigDecimal subtotal) {
        OrderItem orderItem = buildOrderItem(order, itemName, BigDecimal.ZERO, quantity, subtotal);
        orderItem.setVariantName(variantName);
        orderItem.setVariantPrice(variantPrice);
        return orderItem;
    }

    // ========== DTO Builders ==========

    public static AddToCartRequest buildAddToCartRequest(Long itemId, Integer quantity) {
        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(quantity);
        return request;
    }

    public static AddToCartRequest buildAddToCartRequestWithVariant(Long itemId, Long variantId, Integer quantity) {
        AddToCartRequest request = buildAddToCartRequest(itemId, quantity);
        request.setVariantId(variantId);
        return request;
    }

    public static AddToCartRequest buildAddToCartRequestWithAddons(Long itemId, List<Long> addonIds, Integer quantity) {
        AddToCartRequest request = buildAddToCartRequest(itemId, quantity);
        request.setAddonIds(addonIds);
        return request;
    }

    public static OtpRequest buildOtpRequest(String email) {
        OtpRequest request = new OtpRequest();
        request.setEmail(email);
        return request;
    }

    public static OtpVerifyRequest buildOtpVerifyRequest(String identifier, String otp) {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setIdentifier(identifier);
        request.setOtp(otp);
        return request;
    }

    public static AdminLoginRequest buildAdminLoginRequest(String username, String password) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    public static CartItem buildCartItemWithVariant(Cart cart, Item item, ItemVariant variant, int quantity, BigDecimal totalPrice) {
        CartItem cartItem = buildCartItem(cart, item, quantity, totalPrice);
        cartItem.setVariant(variant);
        return cartItem;
    }

    public static CartItem buildCartItemWithAddons(Cart cart, Item item, List<ItemAddon> addons, int quantity, BigDecimal totalPrice) {
        CartItem cartItem = buildCartItem(cart, item, quantity, totalPrice);
        for (ItemAddon addon : addons) {
            CartItemAddon cartItemAddon = new CartItemAddon();
            cartItemAddon.setCartItem(cartItem);
            cartItemAddon.setAddon(addon);
            cartItem.getAddons().add(cartItemAddon);
        }
        return cartItem;
    }
}
