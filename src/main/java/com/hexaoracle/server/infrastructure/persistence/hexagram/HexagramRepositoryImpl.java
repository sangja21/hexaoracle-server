package com.hexaoracle.server.infrastructure.persistence.hexagram;

import com.hexaoracle.server.application.hexagram.dto.HexagramDetailDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramLineDto;
import com.hexaoracle.server.domain.hexagram.port.HexagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class HexagramRepositoryImpl implements HexagramRepository {

    private final HexagramJpaRepository jpaRepository;
    private final HexagramLineJpaRepository lineJpaRepository;

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

    @Override
    public Optional<HexagramDetailDto> findByNo(short no, String locale) {
        // 1) 헥사그램 단건 조회
        HexagramEntity hexagram = jpaRepository.findById(no)
                .orElse(null);
        if (hexagram == null) return Optional.empty();

        // 2) 라인 6개 조회
        List<HexagramLineDto> lines = lineJpaRepository.findByHexagramId(no).stream()
                .map(HexagramLineDto::fromEntity)
                .toList();

        // 3) DTO 조합
        return Optional.of(new HexagramDetailDto(
                hexagram.getId(),
                hexagram.getNameKo(),
                hexagram.getNameEn(),
                hexagram.getNameZh(),
                hexagram.getLinesBits(),
                hexagram.getUpperTrigram(),
                hexagram.getLowerTrigram(),
                hexagram.getJudgement(),
                hexagram.getImage(),
                lines
        ));
    }

    // 공통 매핑 메서드
    private HexagramDto toDto(HexagramEntity e) {
        return new HexagramDto(
                (short) e.getId(),
                e.getNameKo(),
                e.getNameEn(),
                e.getNameZh(),
                e.getLinesBits(),
                e.getUpperTrigram(),
                e.getLowerTrigram(),
                e.getJudgement(),
                e.getImage()
        );
    }
}
