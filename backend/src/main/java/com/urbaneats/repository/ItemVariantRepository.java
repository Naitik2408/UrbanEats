package com.urbaneats.repository;

import com.urbaneats.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ItemVariant entity.
 * Provides CRUD operations for item variants.
 */
@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {
}
