package com.urbaneats.controller;

import com.urbaneats.dto.ItemRequest;
import com.urbaneats.dto.ItemResponse;
import com.urbaneats.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing items with variants and addons.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminItemController {

    private final ItemService itemService;

    /**
     * Create a new item with variants and/or addons.
     * POST /api/admin/items
     *
     * @param request item request DTO
     * @return created item response
     */
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing item with variants and/or addons.
     * PUT /api/admin/items/{id}
     *
     * @param id item ID
     * @param request item request DTO
     * @return updated item response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an item.
     * DELETE /api/admin/items/{id}
     *
     * @param id item ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all items with pagination.
     * GET /api/admin/items
     *
     * @param pageable pagination parameters (default: page=0, size=20, sort by name)
     * @return page of item responses
     */
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getAllItems(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ItemResponse> items = itemService.getAllItems(pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by restaurant with pagination.
     * GET /api/admin/items/restaurant/{restaurantId}
     *
     * @param restaurantId restaurant ID
     * @param pageable pagination parameters (default: page=0, size=20, sort by name)
     * @return page of item responses
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<ItemResponse>> getItemsByRestaurant(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ItemResponse> items = itemService.getItemsByRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get item by ID.
     * GET /api/admin/items/{id}
     *
     * @param id item ID
     * @return item response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(response);
    }
}
