package com.hexaoracle.server.domain.hexagram.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

/**
 * Lines (6효)
 * - 주역의 괘를 구성하는 6개의 효
 * - 하효(index=0) → 상효(index=5) 순서
 */
public record Lines(List<Line> values) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    /** JSON 직렬화 (예: [6,7,8,9,7,6]) */
    public String toJson() {
        try {
            List<Integer> ints = values.stream()
                    .map(Line::getValue)
                    .toList();
            return MAPPER.writeValueAsString(ints);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Lines to JSON", e);
        }
    }

    /** JSON 역직렬화 */
    public static Lines fromJson(String json) {
        try {
            List<Integer> ints = MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));
            return new Lines(ints.stream().map(Line::new).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize Lines from JSON", e);
        }
    }
}
