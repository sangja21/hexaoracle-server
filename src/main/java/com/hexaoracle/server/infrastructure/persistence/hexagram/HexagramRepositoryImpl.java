package com.hexaoracle.server.infrastructure.persistence.hexagram;

import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import com.hexaoracle.server.domain.hexagram.port.HexagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class HexagramRepositoryImpl implements HexagramRepository {

    private final HexagramJpaRepository jpaRepository;

    @Override
    public List<HexagramDto> findAll() {
        return jpaRepository.findAll()   // ✅ 여기서 반환하는 건 List<HexagramEntity>
                .stream()
                .map(e -> new HexagramDto(
                        e.getId(),
                        e.getNameKo(),
                        e.getNameEn(),
                        e.getNameZh(),
                        e.getLinesBits(),
                        e.getUpperTrigram(),
                        e.getLowerTrigram(),
                        e.getJudgement(),
                        e.getImage()
                ))
                .toList();
    }

    @Override
    public List<HexagramDto> findByCriteria(String q, String locale, String cursor, int limit) {
        Integer cursorVal = (cursor != null) ? Integer.valueOf(cursor) : null;

        return jpaRepository.findByCriteria(q, locale, cursorVal, limit).stream()
                .map(e -> new HexagramDto(
                        e.getId(),
                        e.getNameKo(),
                        e.getNameEn(),
                        e.getNameZh(),
                        e.getLinesBits(),
                        e.getUpperTrigram(),
                        e.getLowerTrigram(),
                        e.getJudgement(),
                        e.getImage()
                ))
                .toList();
    }
}
