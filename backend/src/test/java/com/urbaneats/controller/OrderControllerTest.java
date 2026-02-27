package com.urbaneats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.OrderResponse;
import com.urbaneats.entity.Order;
import com.urbaneats.exception.EmptyCartException;
import com.urbaneats.exception.CancellationWindowExpiredException;
import com.urbaneats.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for OrderController.
 * Tests HTTP endpoints for order operations with security.
 */
@WebMvcTest(OrderController.class)
@Import(TestSecurityConfig.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    // ========== Place Order Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/orders - Should place order successfully")
    void shouldPlaceOrderSuccessfully() throws Exception {
        // Arrange
        OrderResponse response = OrderResponse.builder()
                .orderId(1L)
                .status(Order.OrderStatus.PLACED)
                .totalAmount(new BigDecimal("28.00"))
                .createdAt(LocalDateTime.now())
                .canBeCancelled(true)
                .items(new ArrayList<>())
                .build();
        
        when(orderService.placeOrder(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(28.00));
        
        verify(orderService).placeOrder(1L);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/orders - Should return 400 when cart is empty")
    void shouldReturn400WhenCartIsEmpty() throws Exception {
        // Arrange
        when(orderService.placeOrder(1L))
                .thenThrow(new EmptyCartException("Cart is empty"));

        // Act & Assert
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    @Test
    @DisplayName("POST /api/orders - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticatedForPlaceOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isUnauthorized());
        
        verify(orderService, never()).placeOrder(anyLong());
    }

    // ========== Cancel Order Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/orders/{orderId}/cancel - Should cancel order successfully")
    void shouldCancelOrderSuccessfully() throws Exception {
        // Arrange
        Long orderId = 1L;
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(Order.OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("28.00"))
                .createdAt(LocalDateTime.now())
                .canBeCancelled(false)
                .items(new ArrayList<>())
                .build();
        
        when(orderService.cancelOrder(orderId, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        verify(orderService).cancelOrder(orderId, 1L);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/orders/{orderId}/cancel - Should return 400 when cancelling after 60 seconds")
    void shouldReturn400WhenCancellingAfter60Seconds() throws Exception {
        // Arrange
        Long orderId = 1L;
        
        when(orderService.cancelOrder(orderId, 1L))
                .thenThrow(new CancellationWindowExpiredException("Order can only be cancelled within 60 seconds"));

        // Act & Assert
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Order can only be cancelled within 60 seconds"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/orders/{orderId}/cancel - Should return 404 when order not found")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        // Arrange
        Long orderId = 999L;
        
        when(orderService.cancelOrder(orderId, 1L))
                .thenThrow(new EntityNotFoundException("Order not found"));

        // Act & Assert
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/orders/{orderId}/cancel - Should return 403 when order doesn't belong to user")
    void shouldReturn403WhenOrderDoesntBelongToUser() throws Exception {
        // Arrange
        Long orderId = 1L;
        
        when(orderService.cancelOrder(orderId, 1L))
                .thenThrow(new IllegalArgumentException("Order does not belong to user"));

        // Act & Assert
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isForbidden());
    }

    // ========== Get Order Details Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/orders/{orderId} - Should get order details successfully")
    void shouldGetOrderDetailsSuccessfully() throws Exception {
        // Arrange
        Long orderId = 1L;
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(Order.OrderStatus.PLACED)
                .totalAmount(new BigDecimal("28.00"))
                .createdAt(LocalDateTime.now())
                .canBeCancelled(true)
                .items(new ArrayList<>())
                .build();
        
        when(orderService.getOrderById(orderId, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(28.00));
        
        verify(orderService).getOrderById(orderId, 1L);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/orders/{orderId} - Should return 404 when order not found")
    void shouldReturn404WhenGettingNonExistentOrder() throws Exception {
        // Arrange
        Long orderId = 999L;
        
        when(orderService.getOrderById(orderId, 1L))
                .thenThrow(new EntityNotFoundException("Order not found"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/orders/{orderId} - Should return 403 when accessing another user's order")
    void shouldReturn403WhenAccessingAnotherUsersOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        
        when(orderService.getOrderById(orderId, 1L))
                .thenThrow(new IllegalArgumentException("Order does not belong to user"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isForbidden());
    }

    // ========== Get User Orders Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/orders - Should get all user orders successfully")
    void shouldGetAllUserOrdersSuccessfully() throws Exception {
        // Arrange
        OrderResponse order1 = OrderResponse.builder()
                .orderId(1L)
                .status(Order.OrderStatus.PLACED)
                .totalAmount(new BigDecimal("28.00"))
                .createdAt(LocalDateTime.now())
                .canBeCancelled(true)
                .items(new ArrayList<>())
                .build();
        
        OrderResponse order2 = OrderResponse.builder()
                .orderId(2L)
                .status(Order.OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("35.00"))
                .createdAt(LocalDateTime.now().minusHours(1))
                .canBeCancelled(false)
                .items(new ArrayList<>())
                .build();
        
        org.springframework.data.domain.Page<OrderResponse> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(order1, order2));
        when(orderService.getOrdersByUser(eq(1L), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(orderService).getOrdersByUser(eq(1L), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/orders - Should return empty array when user has no orders")
    void shouldReturnEmptyArrayWhenUserHasNoOrders() throws Exception {
        // Arrange
        org.springframework.data.domain.Page<OrderResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(new ArrayList<>());
        when(orderService.getOrdersByUser(eq(1L), any(org.springframework.data.domain.Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/orders - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticatedForGetOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
        
        verify(orderService, never()).getOrdersByUser(anyLong(), any(org.springframework.data.domain.Pageable.class));
    }
}
