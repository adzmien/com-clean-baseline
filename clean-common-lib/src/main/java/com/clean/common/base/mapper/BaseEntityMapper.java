package com.clean.common.base.mapper;

import java.util.List;

import org.mapstruct.MappingTarget;

import com.clean.common.base.dto.OBBaseDTO;

public interface BaseEntityMapper<E, D extends OBBaseDTO> {

    D toDto(E entity);

    E toEntity(D dto);

    void updateEntityFromDto(D dto, @MappingTarget E entity);

    List<D> toDtoList(List<E> entities);

    List<E> toEntityList(List<D> dtos);
}
