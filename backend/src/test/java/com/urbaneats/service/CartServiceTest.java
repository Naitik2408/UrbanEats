package com.urbaneats.service;

import com.urbaneats.TestDataBuilder;
import com.urbaneats.dto.AddToCartRequest;
import com.urbaneats.dto.CartResponse;
import com.urbaneats.entity.*;
import com.urbaneats.exception.InvalidAddonException;
import com.urbaneats.exception.InvalidVariantException;
import com.urbaneats.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartService.
 * Tests cart operations, validation, price calculation, and ownership verification.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemVariantRepository itemVariantRepository;

    @Mock
    private ItemAddonRepository itemAddonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PriceCalculatorService priceCalculatorService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private City testCity;
    private Restaurant testRestaurant;
    private Item simpleItem;
    private Item itemWithVariants;
    private Item itemWithAddons;
    private ItemVariant variant;
    private ItemAddon addon;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildCustomerUser();
        testCart = TestDataBuilder.buildCart(testUser);
        testCity = TestDataBuilder.buildCity();
        testRestaurant = TestDataBuilder.buildRestaurant(testCity);
        
        simpleItem = TestDataBuilder.buildSimpleItem(testRestaurant);
        itemWithVariants = TestDataBuilder.buildItemWithVariants(testRestaurant);
        itemWithAddons = TestDataBuilder.buildItemWithAddons(testRestaurant);
        
        variant = TestDataBuilder.buildVariant(itemWithVariants, "Large", new BigDecimal("15.00"));
        addon = TestDataBuilder.buildAddon(itemWithAddons, "Extra Cheese", new BigDecimal("1.50"));
    }

    // ========== Add Simple Item Tests ==========

    @Test
    @DisplayName("Should add simple item to cart successfully")
    void shouldAddSimpleItemToCart() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(simpleItem.getId(), 2);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(simpleItem.getId())).thenReturn(Optional.of(simpleItem));
        when(priceCalculatorService.calculateCartItemPrice(any(), any(), any(), eq(2)))
                .thenReturn(new BigDecimal("16.00"));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartTotal(any())).thenReturn(new BigDecimal("16.00"));

        // Act
        CartResponse response = cartService.addItemToCart(userId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(itemRepository).findById(simpleItem.getId());
        verify(priceCalculatorService).calculateCartItemPrice(eq(simpleItem), isNull(), any(), eq(2));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should create cart if not exists when adding item")
    void shouldCreateCartIfNotExists() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(simpleItem.getId(), 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(simpleItem.getId())).thenReturn(Optional.of(simpleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(priceCalculatorService.calculateCartItemPrice(any(), any(), any(), anyInt()))
                .thenReturn(new BigDecimal("8.00"));
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartTotal(any())).thenReturn(BigDecimal.ZERO);

        // Act
        CartResponse response = cartService.addItemToCart(userId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).findById(userId);
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    void shouldThrowExceptionWhenItemNotFound() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(999L, 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Item not found");
        
        verify(cartRepository, never()).save(any());
    }

    // ========== Add Item with Variant Tests ==========

    @Test
    @DisplayName("Should add item with variant successfully")
    void shouldAddItemWithVariant() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithVariant(
                itemWithVariants.getId(), variant.getId(), 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithVariants.getId())).thenReturn(Optional.of(itemWithVariants));
        when(itemVariantRepository.findById(variant.getId())).thenReturn(Optional.of(variant));
        when(priceCalculatorService.calculateCartItemPrice(any(), any(), any(), eq(1)))
                .thenReturn(new BigDecimal("15.00"));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartTotal(any())).thenReturn(new BigDecimal("15.00"));

        // Act
        CartResponse response = cartService.addItemToCart(userId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(itemVariantRepository).findById(variant.getId());
        verify(priceCalculatorService).calculateCartItemPrice(eq(itemWithVariants), eq(variant), any(), eq(1));
    }

    @Test
    @DisplayName("Should throw exception when variant required but not provided")
    void shouldThrowExceptionWhenVariantRequiredButNotProvided() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequest(itemWithVariants.getId(), 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithVariants.getId())).thenReturn(Optional.of(itemWithVariants));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(InvalidVariantException.class)
                .hasMessageContaining("Variant is required");
        
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when variant not found")
    void shouldThrowExceptionWhenVariantNotFound() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithVariant(
                itemWithVariants.getId(), 999L, 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithVariants.getId())).thenReturn(Optional.of(itemWithVariants));
        when(itemVariantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Variant not found");
    }

    @Test
    @DisplayName("Should throw exception when variant doesn't belong to item")
    void shouldThrowExceptionWhenVariantDoesntBelongToItem() {
        // Arrange
        Long userId = testUser.getId();
        Item otherItem = TestDataBuilder.buildItemWithVariants(testRestaurant);
        otherItem.setId(99L);
        ItemVariant otherVariant = TestDataBuilder.buildVariant(otherItem, "Small", new BigDecimal("12.00"));
        
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithVariant(
                itemWithVariants.getId(), otherVariant.getId(), 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithVariants.getId())).thenReturn(Optional.of(itemWithVariants));
        when(itemVariantRepository.findById(otherVariant.getId())).thenReturn(Optional.of(otherVariant));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(InvalidVariantException.class)
                .hasMessageContaining("Variant does not belong to item");
    }

    @Test
    @DisplayName("Should throw exception when variant provided for item without variants")
    void shouldThrowExceptionWhenVariantProvidedForItemWithoutVariants() {
        // Arrange
        Long userId = testUser.getId();
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithVariant(
                simpleItem.getId(), variant.getId(), 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(simpleItem.getId())).thenReturn(Optional.of(simpleItem));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(InvalidVariantException.class)
                .hasMessageContaining("Item does not support variants");
    }

    // ========== Add Item with Addons Tests ==========

    @Test
    @DisplayName("Should add item with addons successfully")
    void shouldAddItemWithAddons() {
        // Arrange
        Long userId = testUser.getId();
        List<Long> addonIds = Arrays.asList(addon.getId());
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithAddons(
                itemWithAddons.getId(), addonIds, 2);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithAddons.getId())).thenReturn(Optional.of(itemWithAddons));
        when(itemAddonRepository.findAllById(addonIds)).thenReturn(Arrays.asList(addon));
        when(priceCalculatorService.calculateCartItemPrice(any(), any(), any(), eq(2)))
                .thenReturn(new BigDecimal("19.00"));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartTotal(any())).thenReturn(new BigDecimal("19.00"));

        // Act
        CartResponse response = cartService.addItemToCart(userId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(itemAddonRepository).findAllById(addonIds);
        verify(priceCalculatorService).calculateCartItemPrice(eq(itemWithAddons), isNull(), anyList(), eq(2));
    }

    @Test
    @DisplayName("Should throw exception when addon doesn't belong to item")
    void shouldThrowExceptionWhenAddonDoesntBelongToItem() {
        // Arrange
        Long userId = testUser.getId();
        Item otherItem = TestDataBuilder.buildItemWithAddons(testRestaurant);
        otherItem.setId(99L);
        ItemAddon otherAddon = TestDataBuilder.buildAddon(otherItem, "Bacon", new BigDecimal("2.00"));
        
        List<Long> addonIds = Arrays.asList(otherAddon.getId());
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithAddons(
                itemWithAddons.getId(), addonIds, 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(itemWithAddons.getId())).thenReturn(Optional.of(itemWithAddons));
        when(itemAddonRepository.findAllById(addonIds)).thenReturn(Arrays.asList(otherAddon));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(InvalidAddonException.class)
                .hasMessageContaining("Addon does not belong to item");
    }

    @Test
    @DisplayName("Should throw exception when addon provided for item without addons")
    void shouldThrowExceptionWhenAddonProvidedForItemWithoutAddons() {
        // Arrange
        Long userId = testUser.getId();
        List<Long> addonIds = Arrays.asList(addon.getId());
        AddToCartRequest request = TestDataBuilder.buildAddToCartRequestWithAddons(
                simpleItem.getId(), addonIds, 1);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(itemRepository.findById(simpleItem.getId())).thenReturn(Optional.of(simpleItem));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                .isInstanceOf(InvalidAddonException.class)
                .hasMessageContaining("Item does not support addons");
    }

    // ========== Update Cart Item Quantity Tests ==========

    @Test
    @DisplayName("Should update cart item quantity successfully")
    void shouldUpdateCartItemQuantity() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, simpleItem, 2, new BigDecimal("16.00"));
        
        when(cartItemRepository.findByIdWithAddons(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartItemPrice(any(), any(), any(), eq(3)))
                .thenReturn(new BigDecimal("24.00"));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.updateCartItemQuantity(userId, cartItem.getId(), 3);

        // Assert
        assertThat(response).isNotNull();
        verify(priceCalculatorService).calculateCartItemPrice(any(), any(), any(), eq(3));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should throw exception when updating quantity to zero or negative")
    void shouldThrowExceptionWhenUpdatingQuantityToZero() {
        // Arrange
        Long userId = testUser.getId();
        Long cartItemId = 1L;

        // Act & Assert
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(userId, cartItemId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
        
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cart item doesn't belong to user")
    void shouldThrowExceptionWhenCartItemDoesntBelongToUser() {
        // Arrange
        Long userId = testUser.getId();
        User otherUser = TestDataBuilder.buildCustomerUserWithId(2L, "other@test.com");
        Cart otherCart = TestDataBuilder.buildCart(otherUser);
        otherCart.setId(2L);
        CartItem cartItem = TestDataBuilder.buildCartItem(otherCart, simpleItem, 2, new BigDecimal("16.00"));
        
        when(cartItemRepository.findByIdWithAddons(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(userId, cartItem.getId(), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cart item does not belong to user");
    }

    // ========== Remove Cart Item Tests ==========

    @Test
    @DisplayName("Should remove cart item successfully")
    void shouldRemoveCartItem() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, simpleItem, 2, new BigDecimal("16.00"));
        testCart.getItems().add(cartItem);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));

        // Act
        CartResponse response = cartService.removeCartItem(userId, cartItem.getId());

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("Should throw exception when removing item that doesn't belong to user")
    void shouldThrowExceptionWhenRemovingItemThatDoesntBelongToUser() {
        // Arrange
        Long userId = testUser.getId();
        User otherUser = TestDataBuilder.buildCustomerUserWithId(2L, "other@test.com");
        Cart otherCart = TestDataBuilder.buildCart(otherUser);
        otherCart.setId(2L);
        CartItem cartItem = TestDataBuilder.buildCartItem(otherCart, simpleItem, 2, new BigDecimal("16.00"));
        
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThatThrownBy(() -> cartService.removeCartItem(userId, cartItem.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cart item does not belong to user");
    }

    // ========== Clear Cart Tests ==========

    @Test
    @DisplayName("Should clear cart successfully")
    void shouldClearCart() {
        // Arrange
        Long userId = testUser.getId();
        testCart.getItems().add(TestDataBuilder.buildCartItem(testCart, simpleItem, 2, new BigDecimal("16.00")));
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCart(userId);

        // Assert
        verify(cartRepository).save(testCart);
        assertThat(testCart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle clearing empty cart")
    void shouldHandleClearingEmptyCart() {
        // Arrange
        Long userId = testUser.getId();
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act & Assert
        assertThatCode(() -> cartService.clearCart(userId))
                .doesNotThrowAnyException();
    }

    // ========== Get Cart Details Tests ==========

    @Test
    @DisplayName("Should get cart details successfully")
    void shouldGetCartDetails() {
        // Arrange
        Long userId = testUser.getId();
        CartItem cartItem = TestDataBuilder.buildCartItem(testCart, simpleItem, 2, new BigDecimal("16.00"));
        testCart.getItems().add(cartItem);
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(priceCalculatorService.calculateCartTotal(testCart.getItems()))
                .thenReturn(new BigDecimal("16.00"));

        // Act
        CartResponse response = cartService.getCartDetails(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItemCount()).isEqualTo(1);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("16.00"));
    }

    @Test
    @DisplayName("Should return empty cart when cart not found")
    void shouldReturnEmptyCartWhenNotFound() {
        // Arrange
        Long userId = testUser.getId();
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.getCartDetails(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItemCount()).isEqualTo(0);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getItems()).isEmpty();
    }
}
