package com.clean.common.base.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import com.clean.common.base.component.DynamicFilterComponent;
import com.clean.common.base.dto.OBBaseDTO;
import com.clean.common.base.dto.OBPageDTO;
import com.clean.common.base.dto.OBPageRequestDTO;
import com.clean.common.base.mapper.BaseEntityMapper;
import com.clean.common.util.PaginationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
public abstract class BaseJpaService<E, D extends OBBaseDTO, R extends OBPageRequestDTO, REPO extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> {

    protected final REPO repository;
    protected final BaseEntityMapper<E, D> mapper;
    private final DynamicFilterComponent<E> filterComponent;

    protected BaseJpaService(REPO repository, BaseEntityMapper<E, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.filterComponent = new DynamicFilterComponent<>();
    }

    public List<D> findAll() {

        List<E> entities = repository.findAll();
        List<D> dtos = mapper.toDtoList(entities);
        log.debug("Retrieved {} records", dtos.size());
        return dtos;
    }

    public OBPageDTO<D> findAll(R requestFilter) {

        Pageable pageable = PaginationUtils.getPageable(requestFilter);
        var entityPage = repository.findAll(pageable);
        var dtoPage = entityPage.map(mapper::toDto);
        log.debug("Retrieved {} records out of {} total", dtoPage.getContent().size(), dtoPage.getTotalElements());
        return new OBPageDTO<>(dtoPage);
    }

    public Optional<D> findByID(Long id){

        if (id == null) {
            log.warn("ID is null");
            return Optional.empty();
        }

        return repository.findById(id)
            .map(entity -> {
                D dto = mapper.toDto(entity);
                log.debug("Found record for ID: {}", id);
                return dto;
            });
    }

    public Optional<D> findByCriteria(R request){

        Specification<E> spec = filterComponent.buildExactSpecification(request);
        return repository.findOne(spec)
            .map(entity -> {
                D dto = mapper.toDto(entity);
                log.debug("Found record matching criteria");
                return dto;
            });
    }

    public List<D> findListByCriteria(R request){

        Specification<E> spec = filterComponent.buildSpecification(request);
        List<E> entities = repository.findAll(spec);
        List<D> dtos = mapper.toDtoList(entities);
        log.debug("Found {} records matching criteria", dtos.size());
        return dtos;
    }

    public OBPageDTO<D> findPageByCriteria(R request){

        Specification<E> spec = filterComponent.buildSpecification(request);
        Pageable pageable = PaginationUtils.getPageable(request);
        var entityPage = repository.findAll(spec, pageable);
        var dtoPage = entityPage.map(mapper::toDto);
        log.debug("Found {} records out of {} total matching criteria", dtoPage.getContent().size(), dtoPage.getTotalElements());
        return new OBPageDTO<>(dtoPage);
    }

    @Transactional
    public D add(D dto){
        if (dto == null) {
            log.error("Cannot add null DTO");
            throw new IllegalArgumentException("DTO cannot be null");
        }

        var entity = mapper.toEntity(dto);
        if (entity == null) {
            log.error("Failed to map DTO to entity");
            throw new IllegalStateException("Mapper failed to convert DTO to entity");
        }

        var savedEntity = repository.save(entity);
        D result = mapper.toDto(savedEntity);
        log.info("Successfully added new record with ID: {}", result.getId());
        return result;
    }

    @Transactional
    public D update(D dto){
        if (dto == null) {
            log.error("Cannot update null DTO");
            throw new IllegalArgumentException("DTO cannot be null");
        }

        Long dtoId = dto.getId();
        if (dtoId == null) {
            log.error("Cannot update DTO without ID");
            throw new IllegalArgumentException("DTO ID cannot be null");
        }

        var existingEntity = repository.findById(dtoId)
            .orElseThrow(() -> {
                log.error("Entity with ID {} not found", dtoId);
                return new EntityNotFoundException("Entity not found with ID: " + dtoId);
            });

        mapper.updateEntityFromDto(dto, existingEntity);
        var savedEntity = repository.save(existingEntity);
        D result = mapper.toDto(savedEntity);
        log.info("Successfully updated record with ID: {}", dtoId);
        return result;
    }
}
