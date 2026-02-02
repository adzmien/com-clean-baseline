package com.clean.common.base.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OBBaseResponseDTO<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Long id;

    private String clientTraceNo;
    private String serverTraceNo;

    private Boolean success;
    private String statusDescription;
    private String statusCode;
    private String message;
    private String referenceNo;

    @Valid
    private T reqData;
}
