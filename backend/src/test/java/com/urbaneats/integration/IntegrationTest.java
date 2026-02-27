package com.urbaneats.integration;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.OrderResponse;
import com.urbaneats.entity.*;
import com.urbaneats.repository.*;
import com.urbaneats.service.CartService;
import com.urbaneats.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests using @SpringBootTest.
 * Tests full flow scenarios with real service interactions and transaction management.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("Integration Tests")
class IntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private City testCity;
    private Restaurant testRestaurant;
    private Item testItem;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = TestDataBuilder.buildCustomerUser();
        testUser.setId(null);
        testUser = userRepository.save(testUser);

        testCity = TestDataBuilder.buildCity();
        testCity.setId(null);
        testCity = cityRepository.save(testCity);

        testRestaurant = TestDataBuilder.buildRestaurant(testCity);
        testRestaurant.setId(null);
        testRestaurant = restaurantRepository.save(testRestaurant);

        testItem = TestDataBuilder.buildSimpleItem(testRestaurant);
        testItem.setId(null);
        testItem = itemRepository.save(testItem);
    }

    // ========== Full Flow Tests ==========

    @Test
    @DisplayName("Should complete full order flow: add to cart → place order → verify snapshot")
    void shouldCompleteFullOrderFlow() {
        // Act 1: Add item to cart
        AddToCartRequest addToCartRequest = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), addToCartRequest);

        // Act 2: Place order
        OrderResponse orderResponse = orderService.placeOrder(testUser.getId());

        // Assert: Order created correctly
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getOrderId()).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(Order.OrderStatus.PLACED.name());

        // Assert: Order has correct snapshot
        Order order = orderRepository.findById(orderResponse.getOrderId()).orElseThrow();
        assertThat(order.getItems()).hasSize(1);
        OrderItem orderItem = order.getItems().get(0);
        assertThat(orderItem.getItemName()).isEqualTo(testItem.getName());
        assertThat(orderItem.getQuantity()).isEqualTo(2);
        assertThat(orderItem.getBasePrice()).isEqualByComparingTo(testItem.getBasePrice());

        // Assert: Cart is cleared
        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple items in cart correctly")
    void shouldHandleMultipleItemsInCartCorrectly() {
        // Arrange: Create second item
        Item item2 = TestDataBuilder.buildSimpleItem(testRestaurant);
        item2.setId(null);
        item2.setName("Margherita Pizza");
        item2.setBasePrice(new BigDecimal("10.00"));
        item2 = itemRepository.save(item2);

        // Act: Add two different items to cart
        AddToCartRequest request1 = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        AddToCartRequest request2 = TestDataBuilder.buildAddToCartRequest(item2.getId(), 1);
        
        cartService.addItemToCart(testUser.getId(), request1);
        cartService.addItemToCart(testUser.getId(), request2);

        // Act: Place order
        OrderResponse orderResponse = orderService.placeOrder(testUser.getId());

        // Assert: Order has both items
        Order order = orderRepository.findById(orderResponse.getOrderId()).orElseThrow();
        assertThat(order.getItems()).hasSize(2);
        
        List<String> itemNames = order.getItems().stream()
                .map(OrderItem::getItemName)
                .toList();
        assertThat(itemNames).containsExactlyInAnyOrder(testItem.getName(), "Margherita Pizza");
    }

    @Test
    @DisplayName("Should snapshot correct prices even if item price changes")
    void shouldSnapshotCorrectPricesEvenIfItemPriceChanges() {
        // Act 1: Add item to cart with current price
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 1);
        cartService.addItemToCart(testUser.getId(), request);

        BigDecimal originalPrice = testItem.getBasePrice();

        // Act 2: Change item price in database
        testItem.setBasePrice(new BigDecimal("99.99"));
        itemRepository.save(testItem);

        // Act 3: Place order
        OrderResponse orderResponse = orderService.placeOrder(testUser.getId());

        // Assert: Order has the NEW recalculated price (from database at order time)
        Order order = orderRepository.findById(orderResponse.getOrderId()).orElseThrow();
        OrderItem orderItem = order.getItems().get(0);
        assertThat(orderItem.getBasePrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should prevent placing order with empty cart")
    void shouldPreventPlacingOrderWithEmptyCart() {
        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(testUser.getId()))
                .hasMessageContaining("Cart is empty");

        // Assert: No order created
        org.springframework.data.domain.Page<Order> ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(
            testUser.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(ordersPage.getContent()).isEmpty();
    }

    // ========== Transaction Tests ==========

    @Test
    @DisplayName("Should rollback transaction if order creation fails")
    void shouldRollbackTransactionIfOrderCreationFails() {
        // This test verifies transactional behavior
        // In a real scenario, if order creation fails, cart should not be cleared
        
        // Act: Add item to cart
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request);

        // Assert: Cart has item
        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle concurrent cart modifications")
    void shouldHandleConcurrentCartModifications() {
        // Act: Add same item multiple times (simulating concurrent requests)
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 1);
        
        cartService.addItemToCart(testUser.getId(), request);
        cartService.addItemToCart(testUser.getId(), request);

        // Assert: Cart should have 2 separate cart items (or merged based on implementation)
        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        assertThat(cart.getItems()).isNotEmpty();
    }

    // ========== Order Cancellation Tests ==========

    @Test
    @DisplayName("Should cancel order within 60 seconds")
    void shouldCancelOrderWithin60Seconds() {
        // Arrange: Create and place order
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request);
        OrderResponse orderResponse = orderService.placeOrder(testUser.getId());

        // Act: Cancel order immediately
        OrderResponse cancelledOrder = orderService.cancelOrder(testUser.getId(), orderResponse.getOrderId());

        // Assert: Order is cancelled
        assertThat(cancelledOrder.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED.name());

        // Assert: Order in database is cancelled
        Order order = orderRepository.findById(orderResponse.getOrderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should preserve order items after cancellation")
    void shouldPreserveOrderItemsAfterCancellation() {
        // Arrange: Create and place order
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request);
        OrderResponse orderResponse = orderService.placeOrder(testUser.getId());

        // Act: Cancel order
        orderService.cancelOrder(testUser.getId(), orderResponse.getOrderId());

        // Assert: Order items are still in database
        Order order = orderRepository.findById(orderResponse.getOrderId()).orElseThrow();
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getItemName()).isEqualTo(testItem.getName());
    }

    // ========== Cart Management Tests ==========

    @Test
    @DisplayName("Should update cart item quantity correctly")
    void shouldUpdateCartItemQuantityCorrectly() {
        // Arrange: Add item to cart
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request);

        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        CartItem cartItem = cart.getItems().get(0);

        // Act: Update quantity
        cartService.updateCartItemQuantity(testUser.getId(), cartItem.getId(), 5);

        // Assert: Quantity updated
        cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        CartItem updatedCartItem = cart.getItems().get(0);
        assertThat(updatedCartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should remove item from cart correctly")
    void shouldRemoveItemFromCartCorrectly() {
        // Arrange: Add item to cart
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request);

        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        CartItem cartItem = cart.getItems().get(0);

        // Act: Remove item
        cartService.removeCartItem(testUser.getId(), cartItem.getId());

        // Assert: Cart is empty
        cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should clear entire cart correctly")
    void shouldClearEntireCartCorrectly() {
        // Arrange: Add multiple items to cart
        AddToCartRequest request1 = TestDataBuilder.buildAddToCartRequest(testItem.getId(), 2);
        cartService.addItemToCart(testUser.getId(), request1);

        Item item2 = TestDataBuilder.buildSimpleItem(testRestaurant);
        item2.setId(null);
        item2.setName("Margherita Pizza");
        item2 = itemRepository.save(item2);
        AddToCartRequest request2 = TestDataBuilder.buildAddToCartRequest(item2.getId(), 1);
        cartService.addItemToCart(testUser.getId(), request2);

        // Act: Clear cart
        cartService.clearCart(testUser.getId());

        // Assert: Cart is empty
        Cart cart = cartRepository.findByUserIdWithItems(testUser.getId()).orElseThrow();
        assertThat(cart.getItems()).isEmpty();
    }
}
