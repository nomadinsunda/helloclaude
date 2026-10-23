package com.example.board.global.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * {@link Page}를 그대로 응답하지 않고 필요한 페이징 정보만 노출하기 위한 응답 DTO.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
