package com.clean.backoffice.service;

import java.util.List;

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
import com.clean.common.base.service.BaseJpaService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing CleanConfig operations.
 * <p>
 * Provides business logic for environment-specific configuration properties
 * stored in TBL_CLEAN_CONFIG table.
 * </p>
 */
@Service
@Slf4j
public class CleanConfigService extends BaseJpaService<CleanConfigEntity, OBConfigDTO, OBConfigFilterDTO, CleanConfigRepository> {

    public CleanConfigService(
            CleanConfigRepository repository,
            CleanConfigMapper mapper,
            DynamicFilterComponent<CleanConfigEntity> filterComponent) {
        super(repository, mapper, filterComponent);
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
    public List<OBConfigDTO> findListByCriteria(OBConfigFilterDTO filter) {
        log.debug("Finding configuration property by criteria as list");
        return super.findListByCriteria(filter);
    }

    @Transactional(readOnly = true)
    public OBPageDTO<OBConfigDTO> findPageByCriteria(OBConfigFilterDTO filter) {
        log.debug("Finding configuration property by criteria as page");
        return super.findPageByCriteria(filter);
    }
}
