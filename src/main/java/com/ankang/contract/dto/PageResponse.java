package com.ankang.contract.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> rows;
    private Long total;

    public PageResponse() {
    }

    public PageResponse(List<T> rows, Long total) {
        this.rows = rows;
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
