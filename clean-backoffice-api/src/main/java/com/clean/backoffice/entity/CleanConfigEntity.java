package com.clean.backoffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for TBL_CLEAN_CONFIG table - environment-specific configuration properties.
 * <p>
 * This entity uses a wide table design with separate value columns for each environment:
 * dev, sit, uat, prod, dr.
 * </p>
 */
@Entity
@Table(name = "TBL_CLEAN_CONFIG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CleanConfigEntity {

    /**
     * Auto-generated surrogate primary key.
     * <p>
     * Maps to: id BIGINT AUTO_INCREMENT PRIMARY KEY
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Property key/name - unique business identifier.
     * <p>
     * Examples: "app.feature.enabled", "db.pool.max-size", "api.timeout.connect"
     * </p>
     * <p>
     * Maps to: prop_key VARCHAR(255) NOT NULL UNIQUE
     * </p>
     */
    @Column(name = "prop_key", length = 255, nullable = false, unique = true)
    private String propKey;

    /**
     * Property value for DEV environment.
     * <p>
     * Maps to: dev_value TEXT
     * </p>
     */
    @Column(name = "dev_value", columnDefinition = "TEXT")
    private String devValue;

    /**
     * Property value for SIT (System Integration Testing) environment.
     * <p>
     * Maps to: sit_value TEXT
     * </p>
     */
    @Column(name = "sit_value", columnDefinition = "TEXT")
    private String sitValue;

    /**
     * Property value for UAT (User Acceptance Testing) environment.
     * <p>
     * Maps to: uat_value TEXT
     * </p>
     */
    @Column(name = "uat_value", columnDefinition = "TEXT")
    private String uatValue;

    /**
     * Property value for PROD (Production) environment.
     * <p>
     * Maps to: prod_value TEXT
     * </p>
     */
    @Column(name = "prod_value", columnDefinition = "TEXT")
    private String prodValue;

    /**
     * Property value for DR (Disaster Recovery) environment.
     * <p>
     * Maps to: dr_value TEXT
     * </p>
     */
    @Column(name = "dr_value", columnDefinition = "TEXT")
    private String drValue;

    /**
     * Description of what this property is for.
     * <p>
     * Examples: "Maximum database connection pool size", "Enable new UI redesign"
     * </p>
     * <p>
     * Maps to: description TEXT
     * </p>
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Category/group for organizing properties.
     * <p>
     * Examples: "database", "feature-flags", "api", "security", "batch-processing"
     * </p>
     * <p>
     * Maps to: category VARCHAR(100)
     * </p>
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * Data type indicator for validation and conversion.
     * <p>
     * Common values: "string", "int", "boolean", "json"
     * </p>
     * <p>
     * Maps to: data_type VARCHAR(50) DEFAULT 'string'
     * </p>
     */
    @Column(name = "data_type", length = 50)
    private String dataType;

    /**
     * Flag indicating if this property contains sensitive data.
     * <p>
     * Set to true for passwords, API keys, encryption keys, etc.
     * These values should be encrypted or masked in logs.
     * </p>
     * <p>
     * Maps to: is_sensitive BOOLEAN DEFAULT FALSE
     * </p>
     */
    @Column(name = "is_sensitive")
    private Boolean isSensitive;

    /**
     * Timestamp when property was created.
     * <p>
     * Maps to: created_on DATETIME DEFAULT CURRENT_TIMESTAMP
     * </p>
     */
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    /**
     * Username who created the property.
     * <p>
     * Maps to: created_by VARCHAR(255)
     * </p>
     */
    @Column(name = "created_by", length = 255, updatable = false)
    private String createdBy;

    /**
     * Timestamp when property was last updated.
     * <p>
     * Maps to: updated_on DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * </p>
     */
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /**
     * Username who last updated the property.
     * <p>
     * Maps to: updated_by VARCHAR(255)
     * </p>
     */
    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    /**
     * Constructor with property key.
     *
     * @param propKey the property key/name
     */
    public CleanConfigEntity(String propKey) {
        this.propKey = propKey;
    }

    @Override
    public String toString() {
        return "CleanConfigEntity{" +
                "id=" + id +
                ", propKey='" + propKey + '\'' +
                ", category='" + category + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isSensitive=" + isSensitive +
                '}';
    }
}
