// domain/hexagram/port/HexagramRepository.java
package com.hexaoracle.server.domain.hexagram.port;

import com.hexaoracle.server.application.hexagram.dto.HexagramDetailDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import java.util.List;
import java.util.Optional;


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

    /**
     * 단일 괘 조회
     *
     * @param no 괘 번호 (1~64)
     * @param locale 로케일 (ko|en)
     * @return 해당 HexagramDto, 없으면 Optional.empty()
     */
    Optional<HexagramDetailDto> findByNo(short no, String locale);
}
