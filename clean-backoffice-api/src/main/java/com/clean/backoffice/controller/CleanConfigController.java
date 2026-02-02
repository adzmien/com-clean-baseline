package com.clean.backoffice.controller;

import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.service.CleanConfigService;
import com.clean.common.base.dto.OBBaseResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for CleanConfig operations.
 * <p>
 * Provides RESTful endpoints for managing environment-specific configuration
 * properties stored in TBL_CLEAN_CONFIG.
 * </p>
 * <p>
 * Base URL: /backoffice/api/v1/config
 * </p>
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
public class CleanConfigController {

    private final CleanConfigService cleanConfigService;

    /**
     * Retrieves all configuration properties.
     * <p>
     * This endpoint fetches all records from TBL_CLEAN_CONFIG including
     * values for all environments (dev, sit, uat, prod, dr).
     * </p>
     *
     * @return ResponseEntity containing OBBaseResponseDTO with list of configuration DTOs
     *
     * @apiNote
     * <pre>
     * GET /backoffice/api/v1/config
     *
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "statusCode": "200",
     *   "statusDescription": "OK",
     *   "message": "Retrieved 15 configuration properties successfully",
     *   "reqData": [
     *     {
     *       "id": 1,
     *       "propKey": "app.feature.enabled",
     *       "devValue": "true",
     *       "sitValue": "false",
     *       "uatValue": "true",
     *       "prodValue": "false",
     *       "drValue": "false",
     *       "description": "Enable new feature",
     *       "category": "feature-flags",
     *       "dataType": "boolean",
     *       "isSensitive": false,
     *       "createdOn": "2025-01-28T10:00:00",
     *       "createdBy": "system",
     *       "updatedOn": "2025-01-28T10:00:00",
     *       "updatedBy": "system"
     *     }
     *   ]
     * }
     *
     * Error Response (500 Internal Server Error):
     * {
     *   "success": false,
     *   "statusCode": "500",
     *   "statusDescription": "Internal Server Error",
     *   "message": "Failed to retrieve configuration properties: Connection timeout",
     *   "reqData": null
     * }
     * </pre>
     */
    @GetMapping("/getAll")
    public ResponseEntity<OBBaseResponseDTO<List<OBConfigDTO>>> getAllConfigs() {
        log.debug("GET /api/v1/config/getAll - Retrieving all configuration properties");

        try {
            List<OBConfigDTO> configs = cleanConfigService.getAll();

            OBBaseResponseDTO<List<OBConfigDTO>> response = OBBaseResponseDTO.<List<OBConfigDTO>>builder()
                    .success(true)
                    .statusCode("200")
                    .statusDescription("OK")
                    .message(String.format("Retrieved %d configuration properties successfully", configs.size()))
                    .reqData(configs)
                    .build();

            log.info("Successfully retrieved {} configuration properties", configs.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving configuration properties", e);

            OBBaseResponseDTO<List<OBConfigDTO>> errorResponse = OBBaseResponseDTO.<List<OBConfigDTO>>builder()
                    .success(false)
                    .statusCode("500")
                    .statusDescription("Internal Server Error")
                    .message("Failed to retrieve configuration properties: " + e.getMessage())
                    .reqData(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
