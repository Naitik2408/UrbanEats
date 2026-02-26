package com.urbaneats.config;

import com.urbaneats.entity.Admin;
import com.urbaneats.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

/**
 * Configuration for initializing default admin user on application startup.
 * Provides enhanced security warnings for production environments.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitConfig {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    /**
     * Initialize default admin if no admin exists.
     * 
     * Default credentials: admin/admin123
     * 
     * CRITICAL SECURITY WARNING:
     * - These default credentials are for DEVELOPMENT ONLY
     * - NEVER use default credentials in production
     * - Change immediately after first login
     * - Consider using environment variables for production admin setup
     */
    @Bean
    public CommandLineRunner initDefaultAdmin() {
        return args -> {
            if (adminRepository.count() == 0) {
                boolean isProduction = Arrays.asList(environment.getActiveProfiles())
                    .contains("prod");
                
                if (isProduction) {
                    log.error("╔════════════════════════════════════════════════════════════╗");
                    log.error("║         CRITICAL SECURITY WARNING - PRODUCTION             ║");
                    log.error("╠════════════════════════════════════════════════════════════╣");
                    log.error("║ Default admin credentials are being created in PRODUCTION! ║");
                    log.error("║                                                            ║");
                    log.error("║ This is a SEVERE SECURITY RISK!                            ║");
                    log.error("║                                                            ║");
                    log.error("║ ACTION REQUIRED:                                           ║");
                    log.error("║ 1. Change admin credentials IMMEDIATELY after startup     ║");
                    log.error("║ 2. Use strong, unique passwords for production            ║");
                    log.error("║ 3. Consider pre-seeding admin via secure deployment       ║");
                    log.error("║ 4. Enable multi-factor authentication                      ║");
                    log.error("╚════════════════════════════════════════════════════════════╝");
                }
                
                Admin defaultAdmin = new Admin();
                defaultAdmin.setUsername(DEFAULT_USERNAME);
                defaultAdmin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                defaultAdmin.setRole(Admin.Role.ROLE_ADMIN);
                
                adminRepository.save(defaultAdmin);
                
                log.warn("========================================");
                log.warn("DEFAULT ADMIN CREATED");
                log.warn("Username: {}", DEFAULT_USERNAME);
                log.warn("Password: ******** (check documentation)");
                log.warn("CHANGE THESE CREDENTIALS IMMEDIATELY!");
                log.warn("========================================");
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Admin users already exist. Skipping default admin creation.");
                }
            }
        };
    }
}
