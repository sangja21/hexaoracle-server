package com.hexaoracle.server.application.hexagram;

import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import com.hexaoracle.server.api.common.dto.PageResponseDTO;
import com.hexaoracle.server.domain.hexagram.port.HexagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ListHexagramsUseCase {

    private final HexagramRepository hexagramRepository;

    public PageResponseDTO<HexagramDto> execute(String q, String locale, String cursor, int limit) {
        // 레포지토리에서 커서 기반 목록 조회
        List<HexagramDto> items = hexagramRepository.findByCriteria(q, locale, cursor, limit);

        // nextCursor 계산 (레포지토리에서 마지막 row 기준)
        String nextCursor = (items.size() == limit)
                ? items.get(items.size() - 1).id().toString()   // ✅ record는 id()
                : null;

        return PageResponseDTO.of(items, nextCursor);
    }
}
