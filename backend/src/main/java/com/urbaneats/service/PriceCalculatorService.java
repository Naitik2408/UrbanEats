package com.urbaneats.service;

import com.urbaneats.entity.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for calculating prices server-side.
 * 
 * CRITICAL SECURITY RULE:
 * Never trust frontend price calculations.
 * All prices must be calculated on the server using database values.
 * 
 * PRICE CALCULATION LOGIC:
 * 1. If variant selected: itemPrice = variant.price
 * 2. Else: itemPrice = item.basePrice
 * 3. addonPrice = sum(addon.price for all addons)
 * 4. singleItemTotal = (itemPrice + addonPrice) * quantity
 */
@Service
public class PriceCalculatorService {

    /**
     * Calculate total price for a cart item.
     * 
     * Formula:
     * - If variant selected: basePrice = variant.price
     * - Else: basePrice = item.basePrice
     * - addonPrice = sum of all addon prices
     * - total = (basePrice + addonPrice) * quantity
     * 
     * @param item Menu item
     * @param variant Selected variant (nullable)
     * @param addons Selected addons
     * @param quantity Quantity
     * @return Total price
     */
    public BigDecimal calculateCartItemPrice(
            Item item,
            ItemVariant variant,
            List<ItemAddon> addons,
            Integer quantity
    ) {
        // Step 1: Determine base item price
        BigDecimal itemPrice;
        if (variant != null) {
            itemPrice = variant.getPrice();
        } else {
            itemPrice = item.getBasePrice();
        }

        // Step 2: Calculate addon price
        BigDecimal addonPrice = BigDecimal.ZERO;
        if (addons != null && !addons.isEmpty()) {
            for (ItemAddon addon : addons) {
                addonPrice = addonPrice.add(addon.getPrice());
            }
        }

        // Step 3: Calculate total price
        BigDecimal singleItemPrice = itemPrice.add(addonPrice);
        return singleItemPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calculate total price for entire cart.
     * 
     * @param cartItems List of cart items
     * @return Total cart price
     */
    public BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            total = total.add(cartItem.getTotalPrice());
        }
        return total;
    }

    /**
     * Calculate subtotal for an order item from cart item.
     * Used during order placement to snapshot prices.
     * 
     * @param cartItem Cart item with loaded addons
     * @param item Menu item
     * @param variant Selected variant (nullable)
     * @param addons Selected addons
     * @return Subtotal for order item
     */
    public BigDecimal calculateOrderItemSubtotal(
            CartItem cartItem,
            Item item,
            ItemVariant variant,
            List<ItemAddon> addons
    ) {
        // Recalculate price from database values (not from cart's totalPrice)
        // This ensures we capture the current price at order time
        return calculateCartItemPrice(item, variant, addons, cartItem.getQuantity());
    }
}
