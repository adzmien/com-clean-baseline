package com.clean.backoffice.service;

import com.clean.backoffice.dao.CleanConfigRepository;
import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.entity.CleanConfigEntity;
import com.clean.backoffice.mapper.CleanConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing CleanConfig operations.
 * <p>
 * Provides business logic for environment-specific configuration properties
 * stored in TBL_CLEAN_CONFIG table.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleanConfigService {

    private final CleanConfigRepository repository;
    private final CleanConfigMapper mapper;

    /**
     * Retrieves all configuration properties from the database.
     * <p>
     * This method fetches all records from TBL_CLEAN_CONFIG and converts them
     * to DTOs including all environment values (dev, sit, uat, prod, dr).
     * </p>
     *
     * @return List of configuration DTOs, empty list if no configurations exist
     */
    @Transactional(readOnly = true)
    public List<OBConfigDTO> getAll() {
        log.debug("Fetching all configuration properties");

        List<CleanConfigEntity> entities = repository.findAll();
        List<OBConfigDTO> dtos = mapper.toDtoList(entities);

        log.info("Retrieved {} configuration properties", dtos.size());
        return dtos;
    }
}
