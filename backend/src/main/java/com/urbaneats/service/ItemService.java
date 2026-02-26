package com.urbaneats.service;

import com.urbaneats.dto.AddonRequest;
import com.urbaneats.dto.ItemRequest;
import com.urbaneats.dto.ItemResponse;
import com.urbaneats.dto.VariantRequest;
import com.urbaneats.entity.Item;
import com.urbaneats.entity.ItemAddon;
import com.urbaneats.entity.ItemVariant;
import com.urbaneats.entity.Restaurant;
import com.urbaneats.repository.ItemRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing items with variants and addons.
 * Handles business logic and entity-DTO conversion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Create a new item with variants and/or addons.
     *
     * @param request item request DTO
     * @return created item response
     * @throws EntityNotFoundException if restaurant not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + request.getRestaurantId()));

        // Validate variants and addons consistency
        validateItemRequest(request);

        Item item = new Item();
        item.setName(request.getName());
        item.setBasePrice(request.getBasePrice());
        item.setHasVariants(request.getHasVariants());
        item.setHasAddons(request.getHasAddons());
        item.setRestaurant(restaurant);

        // Add variants if applicable
        if (request.getHasVariants() && request.getVariants() != null) {
            for (VariantRequest variantReq : request.getVariants()) {
                ItemVariant variant = new ItemVariant();
                variant.setName(variantReq.getName());
                variant.setPrice(variantReq.getPrice());
                item.addVariant(variant);
            }
        }

        // Add addons if applicable
        if (request.getHasAddons() && request.getAddons() != null) {
            for (AddonRequest addonReq : request.getAddons()) {
                ItemAddon addon = new ItemAddon();
                addon.setName(addonReq.getName());
                addon.setPrice(addonReq.getPrice());
                item.addAddon(addon);
            }
        }

        Item savedItem = itemRepository.save(item);
        
        if (log.isInfoEnabled()) {
            log.info("Item created: {} in restaurant {} (ID: {}, Variants: {}, Addons: {})", 
                savedItem.getName(), restaurant.getName(), savedItem.getId(), 
                savedItem.getVariants().size(), savedItem.getAddons().size());
        }

        return mapToResponse(savedItem);
    }

    /**
     * Update an existing item with variants and/or addons.
     *
     * @param id item ID
     * @param request item request DTO
     * @return updated item response
     * @throws EntityNotFoundException if item or restaurant not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with ID: " + id));

        // Validate restaurant exists if changed
        if (!item.getRestaurant().getId().equals(request.getRestaurantId())) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + request.getRestaurantId()));
            item.setRestaurant(restaurant);
        }

        // Validate variants and addons consistency
        validateItemRequest(request);

        item.setName(request.getName());
        item.setBasePrice(request.getBasePrice());
        item.setHasVariants(request.getHasVariants());
        item.setHasAddons(request.getHasAddons());

        // Update variants
        item.getVariants().clear();
        if (request.getHasVariants() && request.getVariants() != null) {
            for (VariantRequest variantReq : request.getVariants()) {
                ItemVariant variant = new ItemVariant();
                variant.setName(variantReq.getName());
                variant.setPrice(variantReq.getPrice());
                item.addVariant(variant);
            }
        }

        // Update addons
        item.getAddons().clear();
        if (request.getHasAddons() && request.getAddons() != null) {
            for (AddonRequest addonReq : request.getAddons()) {
                ItemAddon addon = new ItemAddon();
                addon.setName(addonReq.getName());
                addon.setPrice(addonReq.getPrice());
                item.addAddon(addon);
            }
        }

        Item updatedItem = itemRepository.save(item);
        
        if (log.isInfoEnabled()) {
            log.info("Item updated: {} (ID: {}, Variants: {}, Addons: {})", 
                updatedItem.getName(), updatedItem.getId(), 
                updatedItem.getVariants().size(), updatedItem.getAddons().size());
        }

        return mapToResponse(updatedItem);
    }

    /**
     * Delete an item.
     * Cascades to delete all variants and addons.
     *
     * @param id item ID
     * @throws EntityNotFoundException if item not found
     */
    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with ID: " + id));

        itemRepository.delete(item);
        
        if (log.isInfoEnabled()) {
            log.info("Item deleted: {} (ID: {})", item.getName(), id);
        }
    }

    /**
     * Get items by restaurant with pagination.
     *
     * @param restaurantId restaurant ID
     * @param pageable pagination parameters
     * @return page of item responses
     * @throws EntityNotFoundException if restaurant not found
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsByRestaurant(Long restaurantId, Pageable pageable) {
        // Validate restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new EntityNotFoundException("Restaurant not found with ID: " + restaurantId);
        }

        return itemRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get all items with pagination.
     *
     * @param pageable pagination parameters
     * @return page of item responses
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get item by ID.
     *
     * @param id item ID
     * @return item response
     * @throws EntityNotFoundException if item not found
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with ID: " + id));
        return mapToResponse(item);
    }

    /**
     * Validate item request for consistency.
     *
     * @param request item request DTO
     * @throws IllegalArgumentException if validation fails
     */
    private void validateItemRequest(ItemRequest request) {
        // If hasVariants is true, variants list must not be empty
        if (Boolean.TRUE.equals(request.getHasVariants())) {
            if (request.getVariants() == null || request.getVariants().isEmpty()) {
                throw new IllegalArgumentException("Variants list cannot be empty when hasVariants is true");
            }
        }

        // If hasAddons is true, addons list must not be empty
        if (Boolean.TRUE.equals(request.getHasAddons())) {
            if (request.getAddons() == null || request.getAddons().isEmpty()) {
                throw new IllegalArgumentException("Addons list cannot be empty when hasAddons is true");
            }
        }

        // If hasVariants is false, ignore variants list (but don't fail)
        // If hasAddons is false, ignore addons list (but don't fail)
    }

    /**
     * Map Item entity to ItemResponse DTO.
     *
     * @param item item entity
     * @return item response DTO
     */
    private ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setBasePrice(item.getBasePrice());
        response.setRestaurantId(item.getRestaurant().getId());
        response.setRestaurantName(item.getRestaurant().getName());
        response.setHasVariants(item.getHasVariants());
        response.setHasAddons(item.getHasAddons());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        // Map variants
        if (item.getVariants() != null && !item.getVariants().isEmpty()) {
            List<ItemResponse.VariantResponse> variantResponses = item.getVariants().stream()
                    .map(variant -> new ItemResponse.VariantResponse(
                            variant.getId(),
                            variant.getName(),
                            variant.getPrice()
                    ))
                    .collect(Collectors.toList());
            response.setVariants(variantResponses);
        }

        // Map addons
        if (item.getAddons() != null && !item.getAddons().isEmpty()) {
            List<ItemResponse.AddonResponse> addonResponses = item.getAddons().stream()
                    .map(addon -> new ItemResponse.AddonResponse(
                            addon.getId(),
                            addon.getName(),
                            addon.getPrice()
                    ))
                    .collect(Collectors.toList());
            response.setAddons(addonResponses);
        }

        return response;
    }
}
