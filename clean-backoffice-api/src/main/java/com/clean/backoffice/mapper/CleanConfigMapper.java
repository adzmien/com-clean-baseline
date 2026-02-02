package com.clean.backoffice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.entity.CleanConfigEntity;
import com.clean.common.base.mapper.BaseEntityMapper;

/**
 * MapStruct mapper for converting between CleanConfigEntity and OBConfigDTO.
 * Auto-generates implementation at compile time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CleanConfigMapper extends BaseEntityMapper<CleanConfigEntity, OBConfigDTO> {

}
