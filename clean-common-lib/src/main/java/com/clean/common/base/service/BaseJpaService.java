package com.clean.common.base.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clean.common.base.dto.OBBaseDTO;
import com.clean.common.base.mapper.BaseEntityMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseJpaService<E, ID, D extends OBBaseDTO> {

    protected abstract JpaRepository<E, ID> repository();

    protected abstract BaseEntityMapper<E, D> mapper();

    public List<D> findAll() {

        List<E> entities = repository().findAll();
        List<D> dtos = mapper().toDtoList(entities);
        log.info("Retrieved {} records", dtos.size());
        return dtos;
    }
}
