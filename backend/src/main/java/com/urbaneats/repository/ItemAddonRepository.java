package com.urbaneats.repository;

import com.urbaneats.entity.ItemAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ItemAddon entity.
 * Provides CRUD operations for item addons.
 */
@Repository
public interface ItemAddonRepository extends JpaRepository<ItemAddon, Long> {
}
