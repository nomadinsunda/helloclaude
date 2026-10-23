package com.example.board.global.common;

import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import java.util.Set;
import org.springframework.data.domain.Sort;

/**
 * 클라이언트가 보낸 sort 파라미터를 화이트리스트로 제한한다.
 */
public final class SortValidator {

    private SortValidator() {
    }

    public static void validate(Sort sort, Set<String> allowedProperties) {
        for (Sort.Order order : sort) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.INVALID_SORT_PROPERTY,
                        "정렬할 수 없는 항목입니다: " + order.getProperty());
            }
        }
    }
}
