package com.clean.common.base.contract;

public interface PaginationRequest {

    Integer getCurrentPage();

    Integer getPageSize();

    String getSort();
}
