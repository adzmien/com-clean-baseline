package com.clean.backoffice.dto;

import com.clean.common.base.dto.OBBaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for CleanConfigEntity - environment-specific configuration properties.
 * <p>
 * This DTO represents configuration properties with separate value columns for
 * each environment:
 * dev, sit, uat, prod, dr.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OBConfigDTO extends OBBaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * Property key/name - unique business identifier.
     * Examples: "app.feature.enabled", "db.pool.max-size", "api.timeout.connect"
     */
    private String propKey;

    /**
     * Property value for DEV environment.
     */
    private String devValue;

    /**
     * Property value for SIT (System Integration Testing) environment.
     */
    private String sitValue;

    /**
     * Property value for UAT (User Acceptance Testing) environment.
     */
    private String uatValue;

    /**
     * Property value for PROD (Production) environment.
     */
    private String prodValue;

    /**
     * Property value for DR (Disaster Recovery) environment.
     */
    private String drValue;

    /**
     * Description of what this property is for.
     * Examples: "Maximum database connection pool size", "Enable new UI redesign"
     */
    private String description;

    /**
     * Category/group for organizing properties.
     * Examples: "database", "feature-flags", "api", "security", "batch-processing"
     */
    private String category;

    /**
     * Data type indicator for validation and conversion.
     * Common values: "string", "int", "boolean", "json"
     */
    private String dataType;

    /**
     * Flag indicating if this property contains sensitive data.
     * Set to true for passwords, API keys, encryption keys, etc.
     */
    private Boolean isSensitive;
}
