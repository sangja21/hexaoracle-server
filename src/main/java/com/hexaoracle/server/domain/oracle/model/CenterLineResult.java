package com.hexaoracle.server.domain.oracle.model;

import java.util.Objects;
import java.util.Set;

public record CenterLineResult(
        CenterHexagram centerHexagram,
        Set<Integer> changeLineSet,
        int centerLine
) {
    public CenterLineResult {
        Objects.requireNonNull(centerHexagram, "centerHexagram");
        Objects.requireNonNull(changeLineSet, "changeLineSet");
        // 방어적 복사 및 불변화
        changeLineSet = Set.copyOf(changeLineSet);
        // centerLine의 유효 범위는 도메인 규칙에 맞춰 조정
        if (centerLine < 1 || centerLine > 6) {
            throw new IllegalArgumentException("centerLine must be between 1 and 6");
        }
    }
}