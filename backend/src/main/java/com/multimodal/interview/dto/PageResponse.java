package com.multimodal.interview.dto;

import lombok.Data;

import java.util.List;

/**
 * PageResponse 数据传输对象。
 */
@Data
public class PageResponse<T> {
    private int page;
    private int size;
    private long total;
    private List<T> list;

    public static <T> PageResponse<T> of(int page, int size, long total, List<T> list) {
        PageResponse<T> response = new PageResponse<>();
        response.setPage(page);
        response.setSize(size);
        response.setTotal(total);
        response.setList(list);
        return response;
    }
}
