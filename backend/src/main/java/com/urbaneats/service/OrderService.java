package com.urbaneats.service;

import com.urbaneats.dto.OrderResponse;
import com.urbaneats.entity.*;
import com.urbaneats.exception.CancellationWindowExpiredException;
import com.urbaneats.exception.EmptyCartException;
import com.urbaneats.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing order operations.
 * 
 * KEY RESPONSIBILITIES:
 * 1. Place orders from cart with snapshot data
 * 2. Handle 60-second cancellation window
 * 3. Ensure transaction safety for order placement
 * 4. Prevent race conditions and double orders
 * 
 * SNAPSHOT DESIGN:
 * Orders capture item names, variant names, addon names, and prices at order time.
 * This prevents issues when menu items are modified/deleted after order placement.
 * 
 * TRANSACTION SAFETY:
 * - Order placement is atomic (all or nothing)
 * - Cart is cleared only after successful order creation
 * - No partial orders or inconsistent state
 * 
 * CONCURRENCY HANDLING:
 * - Empty cart check prevents double orders from rapid clicks
 * - Transaction isolation ensures consistent cart state
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final ItemAddonRepository itemAddonRepository;
    private final PriceCalculatorService priceCalculatorService;

    /**
     * Place order from cart.
     * 
     * CRITICAL TRANSACTION BOUNDARY:
     * This entire operation must be atomic:
     * 1. Fetch cart with items
     * 2. Validate cart not empty
     * 3. Recalculate prices from database (security)
     * 4. Create order with snapshot data
     * 5. Clear cart
     * 
     * If any step fails, entire operation is rolled back.
     * 
     * CONCURRENCY SAFETY:
     * Empty cart check at start prevents double orders from rapid clicks.
     * If user clicks "Place Order" twice rapidly, second click will fail
     * because cart is already empty (cleared by first transaction).
     */
    @Transactional
    public OrderResponse placeOrder(Long userId) {
        log.info("Placing order - userId: {}", userId);

        // Step 1: Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Step 2: Fetch cart with items
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        log.info("Cart items count: {}", cart.getItems().size());

        // Step 3: Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PLACED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Step 4: Process each cart item and create order items with snapshot data
        for (CartItem cartItem : cart.getItems()) {
            // Fetch fresh data from database for security
            Item item = itemRepository.findById(cartItem.getItem().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));

            ItemVariant variant = null;
            if (cartItem.getVariant() != null) {
                variant = itemVariantRepository.findById(cartItem.getVariant().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
            }

            List<ItemAddon> addons = cartItem.getAddons().stream()
                    .map(cartItemAddon -> itemAddonRepository.findById(cartItemAddon.getAddon().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Addon not found")))
                    .collect(Collectors.toList());

            // Recalculate price from database values (security - never trust cart values)
            BigDecimal subtotal = priceCalculatorService.calculateOrderItemSubtotal(
                    cartItem, item, variant, addons
            );

            // Create order item with snapshot data
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItemName(item.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);

            // Snapshot variant data
            if (variant != null) {
                orderItem.setVariantName(variant.getName());
                orderItem.setVariantPrice(variant.getPrice());
                orderItem.setBasePrice(BigDecimal.ZERO); // Variant price is used
            } else {
                orderItem.setBasePrice(item.getBasePrice());
            }

            // Snapshot addon data
            for (ItemAddon addon : addons) {
                OrderItemAddon orderItemAddon = new OrderItemAddon();
                orderItemAddon.setOrderItem(orderItem);
                orderItemAddon.setAddonName(addon.getName());
                orderItemAddon.setAddonPrice(addon.getPrice());
                orderItem.addAddon(orderItemAddon);
            }

            order.addItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        // Step 5: Save order
        Order savedOrder = orderRepository.save(order);

        log.info("Order placed successfully - orderId: {}, totalAmount: {}", 
                savedOrder.getId(), totalAmount);

        // Step 6: Clear cart (only after successful order creation)
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cart cleared after order placement - userId: {}", userId);

        return mapToOrderResponse(savedOrder);
    }

    /**
     * Cancel order within 60-second window.
     * 
     * CANCELLATION RULES:
     * - Order can only be cancelled within 60 seconds of creation
     * - Order must be in PLACED status
     * - User must own the order
     * 
     * TRANSACTION BOUNDARY:
     * Ensures order status update is atomic.
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order - orderId: {}, userId: {}", orderId, userId);

        // Fetch order and verify ownership
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                throw new IllegalArgumentException("Order is already cancelled");
            } else {
                throw new CancellationWindowExpiredException(
                        "Order can only be cancelled within 60 seconds of placement"
                );
            }
        }

        // Cancel order
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order cancelled successfully - orderId: {}", orderId);

        return mapToOrderResponse(savedOrder);
    }

    /**
     * Get orders for user with pagination.
     * Orders are sorted by creation date descending (newest first).
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        log.info("Fetching orders - userId: {}", userId);

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Get single order details.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        log.info("Fetching order details - orderId: {}, userId: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        return mapToOrderResponseWithItems(order);
    }

    /**
     * Map Order entity to OrderResponse DTO (without items).
     */
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .canBeCancelled(order.canBeCancelled())
                .build();
    }

    /**
     * Map Order entity to OrderResponse DTO (with items).
     */
    private OrderResponse mapToOrderResponseWithItems(Order order) {
        List<OrderResponse.OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .canBeCancelled(order.canBeCancelled())
                .items(itemDtos)
                .build();
    }

    /**
     * Map OrderItem entity to DTO.
     */
    private OrderResponse.OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        List<OrderResponse.OrderItemAddonDto> addonDtos = orderItem.getAddons().stream()
                .map(addon -> OrderResponse.OrderItemAddonDto.builder()
                        .addonName(addon.getAddonName())
                        .addonPrice(addon.getAddonPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.OrderItemDto.builder()
                .orderItemId(orderItem.getId())
                .itemName(orderItem.getItemName())
                .basePrice(orderItem.getBasePrice())
                .variantName(orderItem.getVariantName())
                .variantPrice(orderItem.getVariantPrice())
                .quantity(orderItem.getQuantity())
                .subtotal(orderItem.getSubtotal())
                .addons(addonDtos)
                .build();
    }
}
