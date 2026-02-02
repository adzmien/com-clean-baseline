package com.clean.common.base.dto;

import java.io.Serializable;

import com.clean.common.base.contract.PaginationRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OBPageRequestDTO implements PaginationRequest, Serializable {

    private static final long serialVersionUID = 1L;

    protected Integer currentPage = 1;

    protected Integer pageSize = 20;

    protected String sort;
}
