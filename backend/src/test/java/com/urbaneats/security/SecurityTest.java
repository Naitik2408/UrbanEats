package com.urbaneats.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbaneats.TestDataBuilder;
import com.urbaneats.controller.*;
import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.CityRequest;
import com.urbaneats.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.urbaneats.repository.UserRepository;
import com.urbaneats.security.SecurityConfig;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for role-based access control.
 * Tests ADMIN-only endpoints, CUSTOMER-only endpoints, unauthorized access, and wrong role access.
 */
@WebMvcTest(controllers = {
    AdminCityController.class,
    AdminRestaurantController.class,
    AdminItemController.class,
    CartController.class,
    OrderController.class
})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Security Tests")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CityService cityService;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    // ========== Unauthorized Access Tests ==========

    @Test
    @DisplayName("Should return 401 when accessing cart without authentication")
    void shouldReturn401WhenAccessingCartWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing admin endpoints without authentication")
    void shouldReturn401WhenAccessingAdminEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/cities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing orders without authentication")
    void shouldReturn401WhenAccessingOrdersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    // ========== Admin-Only Endpoint Tests ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should allow ADMIN to access admin endpoints")
    void shouldAllowAdminToAccessAdminEndpoints() throws Exception {
        org.springframework.data.domain.Page<com.urbaneats.dto.CityResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList());
        when(cityService.getAllCities(any(org.springframework.data.domain.Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/admin/cities"))
                .andExpect(status().isOk());
        
        verify(cityService).getAllCities(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should block CUSTOMER from accessing admin endpoints")
    void shouldBlockCustomerFromAccessingAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/cities"))
                .andExpect(status().isForbidden());
        
        verify(cityService, never()).getAllCities(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should allow ADMIN to create city")
    void shouldAllowAdminToCreateCity() throws Exception {
        CityRequest request = new CityRequest();
        request.setName("Mumbai");
        
        when(cityService.createCity(any(CityRequest.class))).thenReturn(TestDataBuilder.buildCityResponse());

        mockMvc.perform(post("/api/admin/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        verify(cityService).createCity(any(CityRequest.class));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should block CUSTOMER from creating city")
    void shouldBlockCustomerFromCreatingCity() throws Exception {
        CityRequest request = new CityRequest();
        request.setName("Mumbai");

        mockMvc.perform(post("/api/admin/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(cityService, never()).createCity(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should allow ADMIN to update city")
    void shouldAllowAdminToUpdateCity() throws Exception {
        CityRequest request = new CityRequest();
        request.setName("Mumbai Updated");
        
        when(cityService.updateCity(eq(1L), any(CityRequest.class))).thenReturn(TestDataBuilder.buildCityResponse());

        mockMvc.perform(put("/api/admin/cities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(cityService).updateCity(eq(1L), any(CityRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should allow ADMIN to delete city")
    void shouldAllowAdminToDeleteCity() throws Exception {
        doNothing().when(cityService).deleteCity(1L);

        mockMvc.perform(delete("/api/admin/cities/1"))
                .andExpect(status().isNoContent());
        
        verify(cityService).deleteCity(1L);
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should block CUSTOMER from deleting city")
    void shouldBlockCustomerFromDeletingCity() throws Exception {
        mockMvc.perform(delete("/api/admin/cities/1"))
                .andExpect(status().isForbidden());
        
        verify(cityService, never()).deleteCity(anyLong());
    }

    // ========== Customer-Only Endpoint Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should allow CUSTOMER to access cart")
    void shouldAllowCustomerToAccessCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should block ADMIN from accessing cart")
    void shouldBlockAdminFromAccessingCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should allow CUSTOMER to add to cart")
    void shouldAllowCustomerToAddToCart() throws Exception {
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(1L, 2);

        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should block ADMIN from adding to cart")
    void shouldBlockAdminFromAddingToCart() throws Exception {
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(1L, 2);

        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(cartService, never()).addItemToCart(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should allow CUSTOMER to place order")
    void shouldAllowCustomerToPlaceOrder() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should block ADMIN from placing order")
    void shouldBlockAdminFromPlacingOrder() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should allow CUSTOMER to view own orders")
    void shouldAllowCustomerToViewOwnOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should block ADMIN from viewing customer orders")
    void shouldBlockAdminFromViewingCustomerOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    // ========== Public Endpoint Tests ==========

    @Test
    @DisplayName("Should allow unauthenticated access to public endpoints")
    void shouldAllowUnauthenticatedAccessToPublicEndpoints() throws Exception {
        // Public endpoints like listing cities, restaurants should be accessible
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    @DisplayName("Should allow CUSTOMER to access public endpoints")
    void shouldAllowCustomerToAccessPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should allow ADMIN to access public endpoints")
    void shouldAllowAdminToAccessPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk());
    }

    // ========== Role Validation Tests ==========

    @Test
    @WithMockUser(username = "1", roles = {})
    @DisplayName("Should block user without roles")
    void shouldBlockUserWithoutRoles() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = {"UNKNOWN"})
    @DisplayName("Should block user with unknown role")
    void shouldBlockUserWithUnknownRole() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    // ========== Authentication Header Tests ==========

    @Test
    @DisplayName("Should return 401 when JWT token is missing")
    void shouldReturn401WhenJwtTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/cart")
                .header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when JWT token is invalid")
    void shouldReturn401WhenJwtTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
