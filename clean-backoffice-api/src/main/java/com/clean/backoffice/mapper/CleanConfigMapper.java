package com.clean.backoffice.mapper;

import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.entity.CleanConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for converting between CleanConfigEntity and OBConfigDTO.
 * Auto-generates implementation at compile time.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface CleanConfigMapper {

    /**
     * Convert entity to DTO.
     * Maps all matching fields including audit fields from OBBaseDTO.
     *
     * @param entity the entity to convert
     * @return the DTO, or null if entity is null
     */
    OBConfigDTO toDto(CleanConfigEntity entity);

    /**
     * Convert DTO to entity for INSERT operations.
     * Note: Audit fields (createdOn, createdBy, updatedOn, updatedBy) should be set by service layer.
     *
     * @param dto the DTO to convert
     * @return the entity, or null if dto is null
     */
    CleanConfigEntity toEntity(OBConfigDTO dto);

    /**
     * Update existing entity from DTO for UPDATE operations.
     * Uses IGNORE strategy to skip null values in DTO.
     * Note: Audit fields should be updated by service layer.
     *
     * @param dto the DTO with updated values
     * @param entity the existing entity to update (modified in place)
     */
    void updateEntityFromDto(OBConfigDTO dto, @MappingTarget CleanConfigEntity entity);

    /**
     * Convert list of entities to list of DTOs.
     *
     * @param entities list of entities
     * @return list of DTOs
     */
    List<OBConfigDTO> toDtoList(List<CleanConfigEntity> entities);

    /**
     * Convert list of DTOs to list of entities.
     *
     * @param dtos list of DTOs
     * @return list of entities
     */
    List<CleanConfigEntity> toEntityList(List<OBConfigDTO> dtos);
}
