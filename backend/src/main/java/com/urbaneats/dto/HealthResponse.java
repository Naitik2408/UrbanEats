package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for health check response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String service;
}
