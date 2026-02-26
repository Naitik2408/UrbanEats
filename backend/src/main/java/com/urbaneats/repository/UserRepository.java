package com.urbaneats.repository;

import com.urbaneats.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number.
     *
     * @param phone the phone number
     * @return Optional containing the user if found
     */
    Optional<User> findByPhone(String phone);
}
