package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed response DTO for Restaurant (customer-facing).
 * Includes city name for context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDetailResponse {

    private Long id;
    private String name;
    private String address;
    private String landmark;
    private Double rating;
    private String cityName;
}
