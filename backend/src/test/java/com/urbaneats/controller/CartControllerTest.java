package com.urbaneats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.CartResponse;
import com.urbaneats.dto.UpdateCartItemRequest;
import com.urbaneats.exception.InvalidAddonException;
import com.urbaneats.exception.InvalidVariantException;
import com.urbaneats.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for CartController.
 * Tests HTTP endpoints for cart operations with security.
 */
@WebMvcTest(CartController.class)
@Import(TestSecurityConfig.class)
@DisplayName("CartController Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    // ========== Add to Cart Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should add item to cart successfully")
    void shouldAddItemToCart() throws Exception {
        // Arrange
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(1L, 2);
        CartResponse response = new CartResponse();
        response.setItemCount(1);
        response.setTotalAmount(new BigDecimal("16.00"));
        response.setItems(new ArrayList<>());
        
        when(cartService.addItemToCart(eq(1L), any(AddToCartRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(16.00));
        
        verify(cartService).addItemToCart(eq(1L), any(AddToCartRequest.class));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should return 400 when item ID is null")
    void shouldReturn400WhenItemIdIsNull() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest();
        request.setQuantity(2);

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should return 400 when quantity is zero")
    void shouldReturn400WhenQuantityIsZero() throws Exception {
        // Arrange
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(1L, 0);

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should return 404 when item not found")
    void shouldReturn404WhenItemNotFound() throws Exception {
        // Arrange
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(999L, 1);
        
        when(cartService.addItemToCart(eq(1L), any(AddToCartRequest.class)))
                .thenThrow(new EntityNotFoundException("Item not found"));

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should return 400 when variant is invalid")
    void shouldReturn400WhenVariantIsInvalid() throws Exception {
        // Arrange
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithVariant(1L, 999L, 1);
        
        when(cartService.addItemToCart(eq(1L), any(AddToCartRequest.class)))
                .thenThrow(new InvalidVariantException("Variant does not belong to item"));

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Variant does not belong to item"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/cart - Should return 400 when addon is invalid")
    void shouldReturn400WhenAddonIsInvalid() throws Exception {
        // Arrange
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithAddons(
                1L, Arrays.asList(999L), 1);
        
        when(cartService.addItemToCart(eq(1L), any(AddToCartRequest.class)))
                .thenThrow(new InvalidAddonException("Addon does not belong to item"));

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Addon does not belong to item"));
    }

    // ========== Update Cart Item Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/cart/items/{cartItemId} - Should update quantity successfully")
    void shouldUpdateQuantitySuccessfully() throws Exception {
        // Arrange
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);
        
        CartResponse response = new CartResponse();
        response.setItemCount(1);
        response.setTotalAmount(new BigDecimal("24.00"));
        
        when(cartService.updateCartItemQuantity(1L, cartItemId, 3)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(24.00));
        
        verify(cartService).updateCartItemQuantity(1L, cartItemId, 3);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/cart/items/{cartItemId} - Should return 400 when quantity invalid")
    void shouldReturn400WhenQuantityInvalid() throws Exception {
        // Arrange
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/cart/items/{cartItemId} - Should return 403 when item doesn't belong to user")
    void shouldReturn403WhenItemDoesntBelongToUser() throws Exception {
        // Arrange
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(2);
        
        when(cartService.updateCartItemQuantity(1L, cartItemId, 2))
                .thenThrow(new IllegalArgumentException("Cart item does not belong to user"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== Remove Cart Item Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("DELETE /api/cart/items/{cartItemId} - Should remove item successfully")
    void shouldRemoveItemSuccessfully() throws Exception {
        // Arrange
        Long cartItemId = 1L;
        CartResponse response = new CartResponse();
        response.setItemCount(0);
        response.setTotalAmount(BigDecimal.ZERO);
        
        when(cartService.removeCartItem(1L, cartItemId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/" + cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
        
        verify(cartService).removeCartItem(1L, cartItemId);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("DELETE /api/cart/items/{cartItemId} - Should return 404 when item not found")
    void shouldReturn404WhenRemovingNonExistentItem() throws Exception {
        // Arrange
        Long cartItemId = 999L;
        
        when(cartService.removeCartItem(1L, cartItemId))
                .thenThrow(new EntityNotFoundException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/" + cartItemId))
                .andExpect(status().isNotFound());
    }

    // ========== Clear Cart Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("DELETE /api/cart - Should clear cart successfully")
    void shouldClearCartSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isNoContent());
        
        verify(cartService).clearCart(1L);
    }

    // ========== Get Cart Details Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("GET /api/cart - Should get cart details successfully")
    void shouldGetCartDetailsSuccessfully() throws Exception {
        // Arrange
        CartResponse response = new CartResponse();
        response.setItemCount(2);
        response.setTotalAmount(new BigDecimal("28.00"));
        response.setItems(new ArrayList<>());
        
        when(cartService.getCartDetails(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.totalAmount").value(28.00));
        
        verify(cartService).getCartDetails(1L);
    }

    @Test
    @DisplayName("GET /api/cart - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
        
        verify(cartService, never()).getCartDetails(anyLong());
    }
}
