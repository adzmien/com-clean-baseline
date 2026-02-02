package com.clean.common.base.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

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
public class OBPageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalRecords;

    private int totalPages;

    private int currentPage;

    private int pageSize;

    @Builder.Default
    private List<T> dataList = new ArrayList<>();

    public OBPageDTO(Page<T> page) {
        this.totalRecords = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.dataList = page.getContent();
    }
}
