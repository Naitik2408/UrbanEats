package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for City.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
