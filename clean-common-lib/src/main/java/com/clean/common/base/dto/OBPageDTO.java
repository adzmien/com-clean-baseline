package com.clean.common.base.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class OBPageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalRecords;
    private int currentPage;
    private int pageSize;
    private String sort;
    private int totalPages;

    @Builder.Default
    private List<T> dataList = new ArrayList<>();
}
