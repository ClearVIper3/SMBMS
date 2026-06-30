package com.viper.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;
    private int total;
    private int pageIndex;
    private int pageSize;
    private int totalPages;

    public static <T> PageResponse<T> of(List<T> records, int total, int pageIndex, int pageSize) {
        PageResponse<T> r = new PageResponse<>();
        r.records = records;
        r.total = total;
        r.pageIndex = pageIndex;
        r.pageSize = pageSize;
        r.totalPages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        return r;
    }
}
