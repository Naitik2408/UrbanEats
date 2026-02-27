package com.urbaneats.service;

import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.CartResponse;
import com.urbaneats.entity.*;
import com.urbaneats.exception.InvalidAddonException;
import com.urbaneats.exception.InvalidVariantException;
import com.urbaneats.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing shopping cart operations.
 * 
 * KEY RESPONSIBILITIES:
 * 1. Validate item, variant, and addon selections
 * 2. Calculate prices server-side (never trust frontend)
 * 3. Manage cart items with proper transaction boundaries
 * 4. Recalculate totals on every modification
 * 
 * VALIDATION RULES:
 * - Item must exist
 * - If item.hasVariants is true, variant must be provided and belong to item
 * - If item.hasVariants is false, variant must not be provided
 * - All addons must belong to item
 * - Quantity must be positive
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final ItemAddonRepository itemAddonRepository;
    private final UserRepository userRepository;
    private final PriceCalculatorService priceCalculatorService;

    /**
     * Get or create cart for user.
     * Each user has only one active cart.
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    /**
     * Add item to cart with validation and price calculation.
     * 
     * TRANSACTION BOUNDARY:
     * This method is transactional to ensure:
     * - Cart creation and item addition are atomic
     * - Price calculation uses consistent database state
     * - No partial cart modifications on failure
     */
    @Transactional
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart - userId: {}, itemId: {}, variantId: {}, quantity: {}",
                userId, request.getItemId(), request.getVariantId(), request.getQuantity());

        // Step 1: Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Step 2: Fetch and validate item
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found with ID: " + request.getItemId()));

        // Step 3: Validate and fetch variant (if applicable)
        ItemVariant variant = null;
        if (item.getHasVariants()) {
            if (request.getVariantId() == null) {
                throw new InvalidVariantException("Variant selection required for this item");
            }
            variant = itemVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new EntityNotFoundException("Variant not found with ID: " + request.getVariantId()));
            
            // Validate variant belongs to item
            if (!variant.getItem().getId().equals(item.getId())) {
                throw new InvalidVariantException("Invalid variant for item: " + item.getName());
            }
        } else {
            if (request.getVariantId() != null) {
                throw new InvalidVariantException("This item does not have variants");
            }
        }

        // Step 4: Validate and fetch addons (if applicable)
        List<ItemAddon> addons = new ArrayList<>();
        if (request.getAddonIds() != null && !request.getAddonIds().isEmpty()) {
            if (!item.getHasAddons()) {
                throw new InvalidAddonException("Addons not allowed for this item");
            }
            
            addons = itemAddonRepository.findAllById(request.getAddonIds());
            if (addons.size() != request.getAddonIds().size()) {
                throw new EntityNotFoundException("One or more addons not found");
            }
            
            // Validate all addons belong to item
            for (ItemAddon addon : addons) {
                if (!addon.getItem().getId().equals(item.getId())) {
                    throw new InvalidAddonException("Addon does not belong to item: " + item.getName());
                }
            }
        }

        // Step 5: Calculate price server-side
        BigDecimal totalPrice = priceCalculatorService.calculateCartItemPrice(
                item, variant, addons, request.getQuantity()
        );

        // Step 6: Create cart item
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setVariant(variant);
        cartItem.setQuantity(request.getQuantity());
        cartItem.setTotalPrice(totalPrice);

        // Step 7: Add addons to cart item
        for (ItemAddon addon : addons) {
            CartItemAddon cartItemAddon = new CartItemAddon();
            cartItemAddon.setCartItem(cartItem);
            cartItemAddon.setAddon(addon);
            cartItem.addAddon(cartItemAddon);
        }

        cart.addItem(cartItem);
        cartRepository.save(cart);

        log.info("Item added to cart successfully - cartItemId: {}, totalPrice: {}", 
                cartItem.getId(), totalPrice);

        return getCartDetails(userId);
    }

    /**
     * Update quantity of cart item and recalculate price.
     * 
     * TRANSACTION BOUNDARY:
     * Ensures quantity update and price recalculation are atomic.
     */
    @Transactional
    public CartResponse updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item quantity - userId: {}, cartItemId: {}, quantity: {}", 
                userId, cartItemId, quantity);

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        CartItem cartItem = cartItemRepository.findByIdWithAddons(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found with ID: " + cartItemId));

        // Verify cart item belongs to user's cart
        Cart cart = getOrCreateCart(userId);
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }

        // Fetch item and variant for price recalculation
        Item item = cartItem.getItem();
        ItemVariant variant = cartItem.getVariant();
        
        // Fetch addons for price recalculation
        List<ItemAddon> addons = cartItem.getAddons().stream()
                .map(CartItemAddon::getAddon)
                .collect(Collectors.toList());

        // Recalculate price with new quantity
        BigDecimal newTotalPrice = priceCalculatorService.calculateCartItemPrice(
                item, variant, addons, quantity
        );

        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(newTotalPrice);
        cartItemRepository.save(cartItem);

        log.info("Cart item quantity updated - cartItemId: {}, newTotalPrice: {}", 
                cartItemId, newTotalPrice);

        return getCartDetails(userId);
    }

    /**
     * Remove item from cart.
     * 
     * TRANSACTION BOUNDARY:
     * Ensures cart item deletion is atomic.
     */
    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item - userId: {}, cartItemId: {}", userId, cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found with ID: " + cartItemId));

        // Verify cart item belongs to user's cart
        Cart cart = getOrCreateCart(userId);
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        log.info("Cart item removed successfully - cartItemId: {}", cartItemId);

        return getCartDetails(userId);
    }

    /**
     * Get cart details with all items.
     * Calculates total amount from all cart items.
     */
    @Transactional(readOnly = true)
    public CartResponse getCartDetails(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .itemCount(0)
                    .build();
        }

        List<CartResponse.CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::mapToCartItemDto)
                .collect(Collectors.toList());

        BigDecimal totalAmount = priceCalculatorService.calculateCartTotal(cart.getItems());

        return CartResponse.builder()
                .items(itemDtos)
                .totalAmount(totalAmount)
                .itemCount(cart.getItems().size())
                .build();
    }

    /**
     * Clear entire cart.
     * 
     * TRANSACTION BOUNDARY:
     * Ensures all cart items are deleted atomically.
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart - userId: {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
            log.info("Cart cleared successfully - userId: {}", userId);
        }
    }

    /**
     * Map CartItem entity to DTO.
     */
    private CartResponse.CartItemDto mapToCartItemDto(CartItem cartItem) {
        Item item = cartItem.getItem();
        ItemVariant variant = cartItem.getVariant();

        CartResponse.VariantDto variantDto = null;
        if (variant != null) {
            variantDto = CartResponse.VariantDto.builder()
                    .variantId(variant.getId())
                    .name(variant.getName())
                    .price(variant.getPrice())
                    .build();
        }

        List<CartResponse.AddonDto> addonDtos = cartItem.getAddons().stream()
                .map(cartItemAddon -> {
                    ItemAddon addon = cartItemAddon.getAddon();
                    return CartResponse.AddonDto.builder()
                            .addonId(addon.getId())
                            .name(addon.getName())
                            .price(addon.getPrice())
                            .build();
                })
                .collect(Collectors.toList());

        return CartResponse.CartItemDto.builder()
                .cartItemId(cartItem.getId())
                .itemId(item.getId())
                .itemName(item.getName())
                .basePrice(item.getBasePrice())
                .variant(variantDto)
                .addons(addonDtos)
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .build();
    }
}
