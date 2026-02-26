package com.urbaneats.controller;

import com.urbaneats.dto.OrderResponse;
import com.urbaneats.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for order management endpoints.
 * All endpoints require CUSTOMER role.
 * 
 * User ID is extracted from JWT token (SecurityContext).
 */
@RestController
@RequestMapping("/api/customer/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Place order from cart.
     * POST /api/customer/order/place
     * 
     * Creates order with snapshot data.
     * Validates cart not empty.
     * Recalculates prices from database.
     * Clears cart after successful order.
     * 
     * CONCURRENCY SAFETY:
     * Empty cart check prevents double orders from rapid clicks.
     * 
     * @param authentication Spring Security authentication
     * @return Order response with order details
     */
    @PostMapping("/place")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Place order request - userId: {}", userId);
        
        OrderResponse response = orderService.placeOrder(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order within 60-second window.
     * POST /api/customer/order/{id}/cancel
     * 
     * Validates:
     * - Order exists and belongs to user
     * - Order is in PLACED status
     * - Cancellation is within 60 seconds
     * 
     * @param id Order ID
     * @param authentication Spring Security authentication
     * @return Order response with updated status
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Cancel order request - userId: {}, orderId: {}", userId, id);
        
        OrderResponse response = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for user.
     * GET /api/customer/orders
     * 
     * Returns paginated list of orders.
     * Orders are sorted by creation date descending (newest first).
     * 
     * @param pageable Pagination parameters
     * @param authentication Spring Security authentication
     * @return Page of order responses
     */
    @GetMapping("s")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Get orders request - userId: {}", userId);
        
        Page<OrderResponse> response = orderService.getOrdersByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get single order details.
     * GET /api/customer/order/{id}
     * 
     * Returns order with all items.
     * 
     * @param id Order ID
     * @param authentication Spring Security authentication
     * @return Order response with items
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Get order details - userId: {}, orderId: {}", userId, id);
        
        OrderResponse response = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(response);
    }
}
