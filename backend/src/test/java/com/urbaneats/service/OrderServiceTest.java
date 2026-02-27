package com.urbaneats.service;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.OrderResponse;
import com.urbaneats.entity.*;
import com.urbaneats.exception.EmptyCartException;
import com.urbaneats.exception.CancellationWindowExpiredException;
import com.urbaneats.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 * Tests order placement, cancellation, snapshot creation, and ownership verification.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemVariantRepository itemVariantRepository;

    @Mock
    private ItemAddonRepository itemAddonRepository;

    @Mock
    private PriceCalculatorService priceCalculatorService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private City testCity;
    private Restaurant testRestaurant;
    private Item item1;
    private Item item2;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildCustomerUser();
        testCart = TestDataBuilder.buildCart(testUser);
        testCity = TestDataBuilder.buildCity();
        testRestaurant = TestDataBuilder.buildRestaurant(testCity);
        item1 = TestDataBuilder.buildSimpleItem(testRestaurant);
        item2 = TestDataBuilder.buildSimpleItem(testRestaurant);
        item2.setId(2L);
        item2.setName("Margherita Pizza");
        testOrder = TestDataBuilder.buildOrder(testUser, new BigDecimal("28.00"));
    }

    // ========== Place Order Tests ==========

    @Test
    @DisplayName("Should place order successfully with cart items")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem1 = TestDataBuilder.buildCartItem(testCart, item1, 2, new BigDecimal("16.00"));
        CartItem cartItem2 = TestDataBuilder.buildCartItem(testCart, item2, 1, new BigDecimal("12.00"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem1, cartItem2)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(item1), isNull(), anyList())).thenReturn(new BigDecimal("16.00"));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(item2), isNull(), anyList())).thenReturn(new BigDecimal("12.00"));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.placeOrder(userId);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).findByUserIdWithItems(userId);
        verify(priceCalculatorService).calculateCartTotal(any());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(testCart); // Cart should be cleared
    }

    @Test
    @DisplayName("Should create order snapshot with correct item details")
    void shouldCreateOrderSnapshotWithCorrectDetails() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, item1, 2, new BigDecimal("16.00"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(item1), isNull(), anyList())).thenReturn(new BigDecimal("16.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(LocalDateTime.now());
            assertThat(order.getItems()).hasSize(1);
            OrderItem orderItem = order.getItems().get(0);
            assertThat(orderItem.getItemName()).isEqualTo(item1.getName());
            assertThat(orderItem.getQuantity()).isEqualTo(2);
            assertThat(orderItem.getBasePrice()).isEqualByComparingTo(new BigDecimal("8.00"));
            assertThat(orderItem.getSubtotal()).isEqualByComparingTo(new BigDecimal("16.00"));
            return order;
        });

        // Act
        OrderResponse response = orderService.placeOrder(userId);

        // Assert
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when placing order with empty cart")
    void shouldThrowExceptionWhenPlacingOrderWithEmptyCart() {
        // Arrange
        Long userId = testUser.getId();
        testCart.setItems(new ArrayList<>());
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(userId))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cart is empty");
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void shouldThrowExceptionWhenCartNotFound() {
        // Arrange
        Long userId = testUser.getId();
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(userId))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("Should clear cart after placing order")
    void shouldClearCartAfterPlacingOrder() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, item1, 2, new BigDecimal("16.00"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(item1), isNull(), anyList())).thenReturn(new BigDecimal("16.00"));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.placeOrder(userId);

        // Assert
        verify(cartRepository).save(argThat(cart -> cart.getItems().isEmpty()));
    }

    @Test
    @DisplayName("Should recalculate total price when placing order")
    void shouldRecalculateTotalPriceWhenPlacingOrder() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, item1, 2, new BigDecimal("16.00"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(item1), isNull(), anyList())).thenReturn(new BigDecimal("18.00")); // Price changed
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);            order.setId(1L);
            order.setCreatedAt(LocalDateTime.now());            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("18.00"));
            return order;
        });

        // Act
        orderService.placeOrder(userId);

        // Assert
        verify(priceCalculatorService).calculateCartTotal(any());
    }

    // ========== Cancel Order Tests ==========

    @Test
    @DisplayName("Should cancel order within 60 seconds successfully")
    void shouldCancelOrderWithin60Seconds() {
        // Arrange
        Long userId = testUser.getId();
        testOrder.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // Created 30 seconds ago
        testOrder.setStatus(Order.OrderStatus.PLACED);
        
        when(orderRepository.findByIdAndUserId(testOrder.getId(), userId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.cancelOrder(testOrder.getId(), userId);

        // Assert
        assertThat(response).isNotNull();
        verify(orderRepository).save(argThat(order -> 
            order.getStatus() == Order.OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("Should throw exception when cancelling order after 60 seconds")
    void shouldThrowExceptionWhenCancellingOrderAfter60Seconds() {
        // Arrange
        Long userId = testUser.getId();
        testOrder.setCreatedAt(LocalDateTime.now().minusSeconds(61)); // Created 61 seconds ago
        testOrder.setStatus(Order.OrderStatus.PLACED);
        
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(testOrder.getId(), userId))
                .isInstanceOf(CancellationWindowExpiredException.class)
                .hasMessageContaining("Order can only be cancelled within 60 seconds");
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled order")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
        // Arrange
        Long userId = testUser.getId();
        testOrder.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        
        when(orderRepository.findByIdAndUserId(testOrder.getId(), userId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(testOrder.getId(), userId))
                .isInstanceOf(CancellationWindowExpiredException.class);
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling completed order")
    void shouldThrowExceptionWhenCancellingCompletedOrder() {
        // Arrange
        Long userId = testUser.getId();
        testOrder.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        
        when(orderRepository.findByIdAndUserId(testOrder.getId(), userId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(testOrder.getId(), userId))
                .isInstanceOf(CancellationWindowExpiredException.class);
    }

    @Test
    @DisplayName("Should throw exception when cancelling order that doesn't belong to user")
    void shouldThrowExceptionWhenCancellingOrderThatDoesntBelongToUser() {
        // Arrange
        Long userId = testUser.getId();
        User otherUser = TestDataBuilder.buildCustomerUserWithId(2L, "other@test.com");
        Order otherOrder = TestDataBuilder.buildOrder(otherUser, new BigDecimal("28.00"));
        otherOrder.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        otherOrder.setStatus(Order.OrderStatus.PLACED);
        
        when(orderRepository.findByIdAndUserId(otherOrder.getId(), userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(otherOrder.getId(), userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found");
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        Long userId = testUser.getId();
        Long orderId = 999L;
        
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    // ========== Get Order Details Tests ==========

    @Test
    @DisplayName("Should get order details successfully")
    void shouldGetOrderDetails() {
        // Arrange
        Long userId = testUser.getId();
        OrderItem orderItem = TestDataBuilder.buildOrderItem(
                testOrder, "Pizza", new BigDecimal("8.00"), 2, new BigDecimal("16.00"));
        testOrder.setItems(Arrays.asList(orderItem));
        
        when(orderRepository.findByIdAndUserId(testOrder.getId(), userId)).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse response = orderService.getOrderById(testOrder.getId(), userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(testOrder.getId());
    }

    @Test
    @DisplayName("Should throw exception when getting order that doesn't belong to user")
    void shouldThrowExceptionWhenGettingOrderThatDoesntBelongToUser() {
        // Arrange
        Long userId = testUser.getId();
        User otherUser = TestDataBuilder.buildCustomerUserWithId(2L, "other@test.com");
        Order otherOrder = TestDataBuilder.buildOrder(otherUser, new BigDecimal("28.00"));
        
        when(orderRepository.findById(otherOrder.getId())).thenReturn(Optional.of(otherOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(otherOrder.getId(), userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order does not belong to user");
    }

    // ========== Get User Orders Tests ==========

    @Test
    @DisplayName("Should get all user orders successfully")
    void shouldGetAllUserOrders() {
        // Arrange
        Long userId = testUser.getId();
        Order order1 = TestDataBuilder.buildOrder(testUser, new BigDecimal("28.00"));
        Order order2 = TestDataBuilder.buildOrder(testUser, new BigDecimal("28.00"));
        Page<Order> ordersPage = new org.springframework.data.domain.PageImpl<>(Arrays.asList(order1, order2));
        
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(org.springframework.data.domain.Pageable.class))).thenReturn(ordersPage);

        // Act
        Page<OrderResponse> responses = orderService.getOrdersByUser(userId, org.springframework.data.domain.Pageable.unpaged());

        // Assert
        assertThat(responses.getContent()).hasSize(2);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(eq(userId), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() {
        // Arrange
        Long userId = testUser.getId();
        Page<Order> emptyPage = new org.springframework.data.domain.PageImpl<>(new ArrayList<>());
        
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(org.springframework.data.domain.Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<OrderResponse> responses = orderService.getOrdersByUser(userId, org.springframework.data.domain.Pageable.unpaged());

        // Assert
        assertThat(responses.getContent()).isEmpty();
    }

    // ========== Order Snapshot Tests ==========

    @Test
    @DisplayName("Should create order snapshot with item variant")
    void shouldCreateOrderSnapshotWithItemVariant() {
        // Arrange
        Long userId = testUser.getId();
        Item itemWithVariant = TestDataBuilder.buildItemWithVariants(testRestaurant);
        ItemVariant variant = TestDataBuilder.buildVariant(itemWithVariant, "Large", new BigDecimal("15.00"));
        CartItem cartItem = TestDataBuilder.buildCartItemWithVariant(
                testCart, itemWithVariant, variant, 1, new BigDecimal("15.00"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemWithVariant.getId())).thenReturn(Optional.of(itemWithVariant));
        when(itemVariantRepository.findById(variant.getId())).thenReturn(Optional.of(variant));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(itemWithVariant), eq(variant), anyList())).thenReturn(new BigDecimal("15.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(LocalDateTime.now());
            OrderItem orderItem = order.getItems().get(0);
            assertThat(orderItem.getVariantName()).isEqualTo("Large");
            assertThat(orderItem.getVariantPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
            return order;
        });

        // Act
        orderService.placeOrder(userId);

        // Assert
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order snapshot with item addons")
    void shouldCreateOrderSnapshotWithItemAddons() {
        // Arrange
        Long userId = testUser.getId();
        Item itemWithAddons = TestDataBuilder.buildItemWithAddons(testRestaurant);
        ItemAddon addon1 = TestDataBuilder.buildAddon(itemWithAddons, "Extra Cheese", new BigDecimal("1.50"));
        ItemAddon addon2 = TestDataBuilder.buildAddon(itemWithAddons, "Bacon", new BigDecimal("2.00"));
        CartItem cartItem = TestDataBuilder.buildCartItemWithAddons(
                testCart, itemWithAddons, Arrays.asList(addon1, addon2), 1, new BigDecimal("11.50"));
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemWithAddons.getId())).thenReturn(Optional.of(itemWithAddons));
        when(itemAddonRepository.findById(addon1.getId())).thenReturn(Optional.of(addon1));
        when(itemAddonRepository.findById(addon2.getId())).thenReturn(Optional.of(addon2));
        when(priceCalculatorService.calculateOrderItemSubtotal(any(), eq(itemWithAddons), isNull(), anyList())).thenReturn(new BigDecimal("11.50"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(LocalDateTime.now());
            OrderItem orderItem = order.getItems().get(0);
            assertThat(orderItem.getAddons()).hasSize(2);
            return order;
        });

        // Act
        orderService.placeOrder(userId);

        // Assert
        verify(orderRepository).save(any(Order.class));
    }
}
