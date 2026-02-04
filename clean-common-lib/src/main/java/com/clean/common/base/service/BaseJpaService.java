package com.clean.common.base.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clean.common.base.component.DynamicFilterComponent;
import com.clean.common.base.dto.OBBaseDTO;
import com.clean.common.base.dto.OBPageDTO;
import com.clean.common.base.dto.OBPageRequestDTO;
import com.clean.common.base.mapper.BaseEntityMapper;
import com.clean.common.util.PaginationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseJpaService<E, ID, D extends OBBaseDTO, R extends OBPageRequestDTO> {

    protected abstract JpaRepository<E, ID> repository();

    protected abstract BaseEntityMapper<E, D> mapper();

    protected abstract DynamicFilterComponent<E> filterComponent();

    protected abstract JpaSpecificationExecutor<E> specificationExecutor();

    public List<D> findAll() {

        List<E> entities = repository().findAll();
        List<D> dtos = mapper().toDtoList(entities);
        log.info("Retrieved {} records", dtos.size());
        return dtos;
    }

    public OBPageDTO<D> findAll(R requestFilter) {

        Pageable pageable = PaginationUtils.getPageable(requestFilter);
        var entityPage = repository().findAll(pageable);
        var dtoPage = entityPage.map(mapper()::toDto);
        log.info("Retrieved {} records out of {} total", dtoPage.getContent().size(), dtoPage.getTotalElements());
        return new OBPageDTO<>(dtoPage);
    }

    public D findByCriteria(R request){
        Specification<E> spec = filterComponent().buildExactSpecification(request);
        var entity = specificationExecutor().findOne(spec).orElse(null);
        D dto = entity != null ? mapper().toDto(entity) : null;
        log.info("Found {} record matching criteria", dto != null ? "1" : "0");
        return dto;
    }
}
