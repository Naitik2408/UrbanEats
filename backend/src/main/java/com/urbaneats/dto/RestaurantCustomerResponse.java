package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight response DTO for Restaurant list (customer-facing).
 * Excludes timestamps and city details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCustomerResponse {

    private Long id;
    private String name;
    private String address;
    private String landmark;
    private Double rating;
}
