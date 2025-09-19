package com.hexaoracle.server.application.hexagram.dto;

import com.hexaoracle.server.infrastructure.persistence.hexagram.HexagramLineEntity;

public record HexagramLineDto(
        short lineNo,
        String textKo,
        String textEn,
        String textZh
) {
    public static HexagramLineDto fromEntity(HexagramLineEntity e) {
        return new HexagramLineDto(
                e.getLineIndex().shortValue(),
                e.getTextKo(),
                e.getTextEn(),
                e.getTextZh()
        );
    }
}
