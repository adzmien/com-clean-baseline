package com.clean.backoffice.dao;

import com.clean.backoffice.entity.CleanConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CleanConfigEntity - environment-specific configuration properties.
 * Provides standard CRUD operations through JpaRepository.
 */
@Repository
public interface CleanConfigRepository extends JpaRepository<CleanConfigEntity, Long> {
}
