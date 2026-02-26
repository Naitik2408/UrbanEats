package com.urbaneats.repository;

import com.urbaneats.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TestEntity to validate JPA configuration.
 * This repository should be removed after infrastructure validation.
 */
@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {
}
