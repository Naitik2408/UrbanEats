package com.urbaneats.controller;

import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.CartResponse;
import com.urbaneats.dto.UpdateCartItemRequest;
import com.urbaneats.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for cart management endpoints.
 * All endpoints require CUSTOMER role.
 * 
 * User ID is extracted from JWT token (SecurityContext).
 */
@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Add item to cart.
     * POST /api/customer/cart/add
     * 
     * Validates item, variant, and addons.
     * Calculates price server-side.
     * 
     * @param request Add to cart request
     * @param authentication Spring Security authentication
     * @return Cart response with updated cart
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Add to cart request - userId: {}, itemId: {}", userId, request.getItemId());
        
        CartResponse response = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update cart item quantity.
     * PUT /api/customer/cart/item/{id}
     * 
     * Recalculates price with new quantity.
     * 
     * @param id Cart item ID
     * @param request Update cart item request
     * @param authentication Spring Security authentication
     * @return Cart response with updated cart
     */
    @PutMapping("/item/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Update cart item quantity - userId: {}, cartItemId: {}, quantity: {}", 
                userId, id, request.getQuantity());
        
        CartResponse response = cartService.updateCartItemQuantity(userId, id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart.
     * DELETE /api/customer/cart/item/{id}
     * 
     * @param id Cart item ID
     * @param authentication Spring Security authentication
     * @return Cart response with updated cart
     */
    @DeleteMapping("/item/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Remove cart item - userId: {}, cartItemId: {}", userId, id);
        
        CartResponse response = cartService.removeCartItem(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cart details.
     * GET /api/customer/cart
     * 
     * Returns all cart items with calculated totals.
     * 
     * @param authentication Spring Security authentication
     * @return Cart response
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Get cart - userId: {}", userId);
        
        CartResponse response = cartService.getCartDetails(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear entire cart.
     * DELETE /api/customer/cart/clear
     * 
     * Removes all items from cart.
     * 
     * @param authentication Spring Security authentication
     * @return No content
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Clear cart - userId: {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
