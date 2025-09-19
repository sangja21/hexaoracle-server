package com.hexaoracle.server.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class PageResponseDTO<T> {
    private final List<T> items;     // 결과 데이터 목록
    private final String nextCursor; // 다음 페이지 커서 (마지막 페이지면 null)
}