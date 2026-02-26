package com.urbaneats.service;

import com.urbaneats.dto.ItemCustomerResponse;
import com.urbaneats.dto.ItemDetailResponse;
import com.urbaneats.entity.Item;
import com.urbaneats.repository.ItemRepository;
import com.urbaneats.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service for customer-facing item operations.
 * Provides read-only access to item data with optimization for variants/addons.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerItemService {

    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Get items by restaurant with pagination.
     * Uses simple query without JOIN FETCH for list view.
     * Variants and addons are NOT loaded - only flags are returned.
     * This avoids N+1 problem while keeping list queries lightweight.
     *
     * @param restaurantId restaurant ID
     * @param pageable pagination parameters
     * @return page of items
     * @throws EntityNotFoundException if restaurant not found
     */
    @Transactional(readOnly = true)
    public Page<ItemCustomerResponse> getItemsByRestaurant(Long restaurantId, Pageable pageable) {
        // Validate restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new EntityNotFoundException("Restaurant not found with ID: " + restaurantId);
        }

        return itemRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToCustomerResponse);
    }

    /**
     * Get item details with variants and addons.
     * Uses JOIN FETCH to load all related data in a SINGLE query.
     * This is critical for detail view to avoid N+1 problem.
     *
     * Why JOIN FETCH here:
     * - Detail view needs variants AND addons
     * - Customer will always view all options together
     * - Single query is more efficient than 3 separate queries
     * - Prevents LazyInitializationException
     *
     * @param itemId item ID
     * @return item details with variants and addons
     * @throws EntityNotFoundException if item not found
     */
    @Transactional(readOnly = true)
    public ItemDetailResponse getItemDetails(Long itemId) {
        Item item = itemRepository.findByIdWithVariantsAndAddons(itemId);
        
        if (item == null) {
            throw new EntityNotFoundException("Item not found with ID: " + itemId);
        }

        return mapToDetailResponse(item);
    }

    /**
     * Map Item entity to lightweight ItemCustomerResponse.
     * Used for list view - excludes variants/addons collections.
     * Only includes flags to indicate if variants/addons exist.
     *
     * @param item item entity
     * @return customer response DTO
     */
    private ItemCustomerResponse mapToCustomerResponse(Item item) {
        return new ItemCustomerResponse(
                item.getId(),
                item.getName(),
                item.getBasePrice(),
                item.getHasVariants(),
                item.getHasAddons()
        );
    }

    /**
     * Map Item entity to ItemDetailResponse with variants and addons.
     * Used for detail view - includes full variant and addon details.
     * Assumes variants and addons are already loaded via JOIN FETCH.
     *
     * @param item item entity (with variants and addons loaded)
     * @return detail response DTO
     */
    private ItemDetailResponse mapToDetailResponse(Item item) {
        ItemDetailResponse response = new ItemDetailResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setBasePrice(item.getBasePrice());
        response.setHasVariants(item.getHasVariants());
        response.setHasAddons(item.getHasAddons());

        // Map variants
        if (item.getVariants() != null && !item.getVariants().isEmpty()) {
            response.setVariants(
                    item.getVariants().stream()
                            .map(variant -> new ItemDetailResponse.VariantDetail(
                                    variant.getId(),
                                    variant.getName(),
                                    variant.getPrice()
                            ))
                            .collect(Collectors.toList())
            );
        }

        // Map addons
        if (item.getAddons() != null && !item.getAddons().isEmpty()) {
            response.setAddons(
                    item.getAddons().stream()
                            .map(addon -> new ItemDetailResponse.AddonDetail(
                                    addon.getId(),
                                    addon.getName(),
                                    addon.getPrice()
                            ))
                            .collect(Collectors.toList())
            );
        }

        return response;
    }
}
