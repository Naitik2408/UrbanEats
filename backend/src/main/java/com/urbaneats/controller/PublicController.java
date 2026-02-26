package com.urbaneats.controller;

import com.urbaneats.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public controller for health check endpoint.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * Health check endpoint to verify service is running.
     *
     * @return Health status response
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "UrbanEats"));
    }
}
