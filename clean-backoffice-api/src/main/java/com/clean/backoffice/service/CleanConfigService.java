package com.clean.backoffice.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clean.backoffice.dao.CleanConfigRepository;
import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.dto.OBConfigFilterDTO;
import com.clean.backoffice.entity.CleanConfigEntity;
import com.clean.backoffice.mapper.CleanConfigMapper;
import com.clean.common.base.component.DynamicFilterComponent;
import com.clean.common.base.dto.OBBaseRequestDTO;
import com.clean.common.base.dto.OBPageDTO;
import com.clean.common.base.mapper.BaseEntityMapper;
import com.clean.common.base.service.BaseJpaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
public class CleanConfigService extends BaseJpaService<CleanConfigEntity, Long, OBConfigDTO, OBConfigFilterDTO> {

    private final CleanConfigRepository repository;
    private final CleanConfigMapper mapper;
    private final DynamicFilterComponent<CleanConfigEntity> filterComponent;

    @Override
    protected JpaRepository<CleanConfigEntity, Long> repository() {
        return repository;
    }

    @Override
    protected BaseEntityMapper<CleanConfigEntity, OBConfigDTO> mapper() {
        return mapper;
    }

    @Override
    protected DynamicFilterComponent<CleanConfigEntity> filterComponent() {
        return filterComponent;
    }

    @Override
    protected JpaSpecificationExecutor<CleanConfigEntity> specificationExecutor() {
        return repository;
    }

    @Transactional(readOnly = true)
    public List<OBConfigDTO> getAll() {
        log.debug("Fetching all configuration properties");
        return super.findAll();
    }

    @Transactional(readOnly = true)
    public OBPageDTO<OBConfigDTO> getAll(OBBaseRequestDTO<OBConfigFilterDTO> request) {

        log.debug("Fetching all configuration properties");
        return super.findAll(request.getReqData());
    }

    @Transactional(readOnly = true)
    public OBConfigDTO findByCriteria(OBConfigFilterDTO filter) {
        log.debug("Finding configuration property by criteria");
        return super.findByCriteria(filter);
    }
}
