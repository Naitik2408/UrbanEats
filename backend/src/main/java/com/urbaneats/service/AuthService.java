package com.urbaneats.service;

import com.urbaneats.dto.*;
import com.urbaneats.entity.Admin;
import com.urbaneats.entity.User;
import com.urbaneats.repository.AdminRepository;
import com.urbaneats.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 * Handles OTP-based customer authentication and admin password authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final OtpService otpService;
    private final FirebaseOtpService firebaseOtpService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Generate and send OTP to customer.
     *
     * @param request the OTP request containing email or phone
     * @return OTP response with message (and OTP for development only)
     */
    public OtpResponse generateOtp(OtpRequest request) {
        String identifier = request.getIdentifier();
        
        // Check if it's a phone number (use Firebase)
        if (identifier.startsWith("+") || identifier.matches("^\\d{10,}$")) {
            // Normalize phone to E.164 format if needed
            String phone = identifier.startsWith("+") ? identifier : "+91" + identifier;
            
            // Generate OTP using Firebase
            String otp = firebaseOtpService.generateOtp(phone);
            
            log.info("Firebase OTP generation requested for phone: {}", phone);
            
            // Return OTP in dev mode only
            String message = "OTP sent to your phone via Firebase";
            return new OtpResponse(message, otp); // OTP for development
        } else {
            // Email-based OTP (existing flow)
            String otp = otpService.generateAndStoreOtp(identifier);
            
            log.info("OTP generation requested for email: {}", identifier);
            
            String message = otp != null 
                ? "OTP sent successfully (Development Mode)" 
                : "OTP sent successfully. Please check your email.";
            
            return new OtpResponse(message, otp);
        }
    }

    /**
     * Verify OTP and authenticate customer.
     * Creates new user if not exists.
     *
     * @param request the OTP verify request
     * @return authentication response with JWT token
     */
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String identifier = request.getIdentifier();
        String otp = request.getOtp();

        // Check if it's a phone number (use Firebase)
        boolean isPhone = identifier.startsWith("+") || identifier.matches("^\\d{10,}$");
        
        if (isPhone) {
            // Normalize phone to E.164 format if needed
            String phone = identifier.startsWith("+") ? identifier : "+91" + identifier;
            
            // Verify OTP using Firebase
            if (!firebaseOtpService.verifyOtp(phone, otp)) {
                throw new BadCredentialsException("Invalid or expired OTP");
            }
            
            // Find or create user with phone
            User user = findOrCreateUser(phone);
            
            // Generate JWT token
            String token = jwtService.generateToken(user.getId(), user.getRole().name());
            
            log.info("User authenticated successfully via Firebase: {}", user.getId());
            
            return new AuthResponse(token, user.getRole().name(), user.getId(), "Authentication successful");
        } else {
            // Email-based OTP verification (existing flow)
            if (!otpService.verifyOtp(identifier, otp)) {
                throw new BadCredentialsException("Invalid or expired OTP");
            }
            
            // Find or create user with email
            User user = findOrCreateUser(identifier);
            
            // Generate JWT token
            String token = jwtService.generateToken(user.getId(), user.getRole().name());
            
            log.info("User authenticated successfully: {}", user.getId());
            
            return new AuthResponse(token, user.getRole().name(), user.getId(), "Authentication successful");
        }
    }

    /**
     * Authenticate admin with username and password.
     *
     * @param request the admin login request
     * @return authentication response with JWT token
     */
    public AuthResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtService.generateToken(admin.getId(), admin.getRole().name());

        log.info("Admin authenticated successfully: {}", admin.getUsername());
        
        return new AuthResponse(token, admin.getRole().name(), admin.getId(), "Admin authentication successful");
    }

    /**
     * Find existing user or create new user.
     *
     * @param identifier email or phone
     * @return the user entity
     */
    private User findOrCreateUser(String identifier) {
        // Determine if identifier is email or phone
        boolean isEmail = identifier.contains("@");
        
        User user;
        if (isEmail) {
            user = userRepository.findByEmail(identifier).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(identifier);
                user.setRole(User.Role.ROLE_CUSTOMER);
                user = userRepository.save(user);
                log.info("New user created with email: {}", identifier);
            }
        } else {
            user = userRepository.findByPhone(identifier).orElse(null);
            if (user == null) {
                user = new User();
                user.setPhone(identifier);
                user.setRole(User.Role.ROLE_CUSTOMER);
                user = userRepository.save(user);
                log.info("New user created with phone: {}", identifier);
            }
        }
        
        return user;
    }
}
