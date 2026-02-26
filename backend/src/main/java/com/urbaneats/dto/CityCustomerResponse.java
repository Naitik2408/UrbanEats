package com.urbaneats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight response DTO for City (customer-facing).
 * Excludes timestamps and internal details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityCustomerResponse {

    private Long id;
    private String name;
}
