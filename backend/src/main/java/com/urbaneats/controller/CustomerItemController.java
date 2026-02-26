package com.urbaneats.controller;

import com.urbaneats.dto.ItemCustomerResponse;
import com.urbaneats.dto.ItemDetailResponse;
import com.urbaneats.service.CustomerItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Customer controller for browsing menu items.
 * Provides read-only access to item data with variants and addons.
 * Public access - no authentication required.
 */
@RestController
@RequestMapping("/api/customer/items")
@RequiredArgsConstructor
public class CustomerItemController {

    private final CustomerItemService customerItemService;

    /**
     * Get items by restaurant with pagination.
     * GET /api/customer/items?restaurantId={restaurantId}
     *
     * Returns lightweight item list WITHOUT variants/addons.
     * Clients should call GET /items/{id} for full details.
     *
     * Query Parameters:
     * - restaurantId (required): Restaurant ID to filter items
     * - page (optional): Page number (default: 0)
     * - size (optional): Page size (default: 20)
     * - sort (optional): Sort field and direction (default: name,ASC)
     *
     * @param restaurantId restaurant ID
     * @param pageable pagination parameters
     * @return page of items
     */
    @GetMapping
    public ResponseEntity<Page<ItemCustomerResponse>> getItemsByRestaurant(
            @RequestParam Long restaurantId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ItemCustomerResponse> items = customerItemService.getItemsByRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get item details with variants and addons.
     * GET /api/customer/items/{id}
     *
     * Returns complete item details INCLUDING all variants and addons.
     * Optimized with JOIN FETCH to load everything in a single query.
     *
     * @param id item ID
     * @return item details with variants and addons
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDetailResponse> getItemDetails(@PathVariable Long id) {
        ItemDetailResponse item = customerItemService.getItemDetails(id);
        return ResponseEntity.ok(item);
    }
}
