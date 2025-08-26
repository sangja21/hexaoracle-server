package com.hexaoracle.server.domain.oracle.model;

import java.util.List;
import java.util.Objects;

/**
 * Lines (6효)
 * - 주역의 괘를 구성하는 6개의 효
 * - 하효(index=0) → 상효(index=5) 순서
 */
public record Lines(List<Line> values) {

    public Lines {
        Objects.requireNonNull(values, "lines cannot be null");
        if (values.size() != 6) {
            throw new IllegalArgumentException("Lines must contain exactly 6 Line values");
        }
        // 방어적 복사 (불변성 보장)
        values = List.copyOf(values);
    }

    /** 0~5 index (0=하효, 5=상효) */
    public Line get(int index) {
        return values.get(index);
    }

    /** 길이 반환 (항상 6) */
    public int size() {
        return values.size();
    }

    /** 하효→상효 부분 슬라이스 */
    public List<Line> subLines(int from, int to) {
        if (from < 0 || to > values.size() || from >= to) {
            throw new IllegalArgumentException("Invalid subLines range: " + from + "," + to);
        }
        return List.copyOf(values.subList(from, to));
    }

    /** 이진 문자열 (하효→상효, 양=1, 음=0) */
    public String toBinary() {
        StringBuilder sb = new StringBuilder();
        for (Line line : values) {
            sb.append(line.isYang() ? "1" : "0");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Lines{" + toBinary() + "}";
    }
}
