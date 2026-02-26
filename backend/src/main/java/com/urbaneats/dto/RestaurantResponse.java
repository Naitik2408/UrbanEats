package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Restaurant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String name;
    private String address;
    private String landmark;
    private Double rating;
    private Long cityId;
    private String cityName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
