// domain/hexagram/port/HexagramRepository.java
package com.hexaoracle.server.domain.hexagram.port;

import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import java.util.List;


public interface HexagramRepository {
    List<HexagramDto> findAll();

    /**
     * 검색/필터 + 커서 기반 페이지네이션 조회
     *
     * @param q 검색어 (nullable)
     * @param locale 로케일 (ko|en)
     * @param cursor 마지막으로 가져온 hexagramId (nullable)
     * @param limit 최대 조회 개수
     * @return HexagramDto 목록
     */
    List<HexagramDto> findByCriteria(String q, String locale, String cursor, int limit);
}
