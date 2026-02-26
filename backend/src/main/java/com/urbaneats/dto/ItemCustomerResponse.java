package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight response DTO for Item list (customer-facing).
 * Excludes variants/addons for list view performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCustomerResponse {

    private Long id;
    private String name;
    private BigDecimal basePrice;
    private Boolean hasVariants;
    private Boolean hasAddons;
}
