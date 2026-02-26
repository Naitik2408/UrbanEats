package com.urbaneats.repository;

import com.urbaneats.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Admin entity operations.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find admin by username.
     *
     * @param username the username
     * @return Optional containing the admin if found
     */
    Optional<Admin> findByUsername(String username);
}
