package com.clean.backoffice.controller;

import com.clean.backoffice.dto.OBConfigDTO;
import com.clean.backoffice.dto.OBConfigFilterDTO;
import com.clean.backoffice.service.CleanConfigService;
import com.clean.common.base.dto.OBBaseRequestDTO;
import com.clean.common.base.dto.OBBaseResponseDTO;
import com.clean.common.base.dto.OBPageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        @PostMapping("/getAll")
        public ResponseEntity<OBBaseResponseDTO<OBPageDTO<OBConfigDTO>>> getConfigsPaginated(
                        @RequestBody @Valid OBBaseRequestDTO<OBConfigFilterDTO> request) {

                log.debug("POST /api/v1/config - Retrieving configs with pagination and filters");

                try {
                        OBPageDTO<OBConfigDTO> page = cleanConfigService.getAll(request);

                        OBBaseResponseDTO<OBPageDTO<OBConfigDTO>> response = OBBaseResponseDTO
                                        .<OBPageDTO<OBConfigDTO>>builder()
                                        .success(true)
                                        .statusCode("200")
                                        .statusDescription("OK")
                                        .message(String.format("Retrieved %d of %d configuration properties",
                                                        page.getDataList().size(), page.getTotalRecords()))
                                        .reqData(page)
                                        .build();

                        log.info("Successfully retrieved page {} with {} records out of {} total",
                                        page.getCurrentPage(), page.getDataList().size(), page.getTotalRecords());
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error retrieving configuration properties with pagination", e);

                        OBBaseResponseDTO<OBPageDTO<OBConfigDTO>> errorResponse = OBBaseResponseDTO
                                        .<OBPageDTO<OBConfigDTO>>builder()
                                        .success(false)
                                        .statusCode("500")
                                        .statusDescription("Internal Server Error")
                                        .message("Failed to retrieve configuration properties: " + e.getMessage())
                                        .reqData(null)
                                        .build();

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }

        @GetMapping("/getAll")
        public ResponseEntity<OBBaseResponseDTO<List<OBConfigDTO>>> getAllConfigs() {
                log.debug("GET /api/v1/config/getAll - Retrieving all configuration properties");

                try {
                        List<OBConfigDTO> configs = cleanConfigService.getAll();

                        OBBaseResponseDTO<List<OBConfigDTO>> response = OBBaseResponseDTO.<List<OBConfigDTO>>builder()
                                        .success(true)
                                        .statusCode("200")
                                        .statusDescription("OK")
                                        .message(String.format("Retrieved %d configuration properties successfully",
                                                        configs.size()))
                                        .reqData(configs)
                                        .build();

                        log.info("Successfully retrieved {} configuration properties", configs.size());
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error retrieving configuration properties", e);

                        OBBaseResponseDTO<List<OBConfigDTO>> errorResponse = OBBaseResponseDTO
                                        .<List<OBConfigDTO>>builder()
                                        .success(false)
                                        .statusCode("500")
                                        .statusDescription("Internal Server Error")
                                        .message("Failed to retrieve configuration properties: " + e.getMessage())
                                        .reqData(null)
                                        .build();

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }

        @PostMapping("/findByCriteria")
        public ResponseEntity<OBBaseResponseDTO<OBConfigDTO>> findByCriteria(
                        @RequestBody @Valid OBBaseRequestDTO<OBConfigFilterDTO> request) {

                log.debug("POST /api/v1/config/findByCriteria - Finding config by criteria");

                try {
                        OBConfigDTO config = cleanConfigService.findByCriteria(request.getReqData()).orElse(null);

                        OBBaseResponseDTO<OBConfigDTO> response = OBBaseResponseDTO.<OBConfigDTO>builder()
                                        .success(true)
                                        .statusCode("200")
                                        .statusDescription("OK")
                                        .message(config != null ? "Configuration found" : "No configuration found matching criteria")
                                        .reqData(config)
                                        .build();

                        log.info("findByCriteria completed: {}", config != null ? "found" : "not found");
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error finding configuration by criteria", e);

                        OBBaseResponseDTO<OBConfigDTO> errorResponse = OBBaseResponseDTO.<OBConfigDTO>builder()
                                        .success(false)
                                        .statusCode("500")
                                        .statusDescription("Internal Server Error")
                                        .message("Failed to find configuration: " + e.getMessage())
                                        .reqData(null)
                                        .build();

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }

        @PostMapping("/findListByCriteria")
        public ResponseEntity<OBBaseResponseDTO<List<OBConfigDTO>>> findListByCriteria(
                        @RequestBody @Valid OBBaseRequestDTO<OBConfigFilterDTO> request) {

                log.debug("POST /api/v1/config/findListByCriteria - Finding configs by criteria as list");

                try {
                        List<OBConfigDTO> configs = cleanConfigService.findListByCriteria(request.getReqData());

                        OBBaseResponseDTO<List<OBConfigDTO>> response = OBBaseResponseDTO.<List<OBConfigDTO>>builder()
                                        .success(true)
                                        .statusCode("200")
                                        .statusDescription("OK")
                                        .message(String.format("Found %d configuration properties matching criteria",
                                                        configs.size()))
                                        .reqData(configs)
                                        .build();

                        log.info("findListByCriteria completed: found {} records", configs.size());
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error finding configurations by criteria as list", e);

                        OBBaseResponseDTO<List<OBConfigDTO>> errorResponse = OBBaseResponseDTO
                                        .<List<OBConfigDTO>>builder()
                                        .success(false)
                                        .statusCode("500")
                                        .statusDescription("Internal Server Error")
                                        .message("Failed to find configurations: " + e.getMessage())
                                        .reqData(null)
                                        .build();

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }

        @PostMapping("/findPageByCriteria")
        public ResponseEntity<OBBaseResponseDTO<OBPageDTO<OBConfigDTO>>> findPageByCriteria(
                        @RequestBody @Valid OBBaseRequestDTO<OBConfigFilterDTO> request) {

                log.debug("POST /api/v1/config/findPageByCriteria - Finding configs by criteria with pagination");

                try {
                        OBPageDTO<OBConfigDTO> page = cleanConfigService.findPageByCriteria(request.getReqData());

                        OBBaseResponseDTO<OBPageDTO<OBConfigDTO>> response = OBBaseResponseDTO
                                        .<OBPageDTO<OBConfigDTO>>builder()
                                        .success(true)
                                        .statusCode("200")
                                        .statusDescription("OK")
                                        .message(String.format("Found %d of %d configuration properties matching criteria",
                                                        page.getDataList().size(), page.getTotalRecords()))
                                        .reqData(page)
                                        .build();

                        log.info("findPageByCriteria completed: page {} with {} records out of {} total",
                                        page.getCurrentPage(), page.getDataList().size(), page.getTotalRecords());
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error finding configurations by criteria with pagination", e);

                        OBBaseResponseDTO<OBPageDTO<OBConfigDTO>> errorResponse = OBBaseResponseDTO
                                        .<OBPageDTO<OBConfigDTO>>builder()
                                        .success(false)
                                        .statusCode("500")
                                        .statusDescription("Internal Server Error")
                                        .message("Failed to find configurations: " + e.getMessage())
                                        .reqData(null)
                                        .build();

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }
}
