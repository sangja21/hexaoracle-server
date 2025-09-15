package com.hexaoracle.server.infrastructure.persistence.oracle;

import com.hexaoracle.server.domain.hexagram.model.Lines;
import com.hexaoracle.server.domain.oracle.model.Oracle;
import com.hexaoracle.server.domain.oracle.model.OracleId;
import com.hexaoracle.server.domain.oracle.model.Question;
import org.springframework.stereotype.Component;

@Component
public class OracleMapper {

    public OracleEntity toEntity(Oracle oracle) {
        return new OracleEntity(
                oracle.getId() != null ? oracle.getId().getValue() : null,
                null, // UserEntity → 서비스에서 주입
                oracle.getQuestion().getValue(),
                oracle.getLines().toJson(),
                null, // linesBits
                null, // movingCount
                null, // originalHexagram → 서비스에서 주입
                null, // changedHexagram → 서비스에서 주입
                oracle.getLocale(),
                oracle.getCreatedAt()
        );
    }


    public Oracle toDomain(OracleEntity entity) {
        return new Oracle(
                entity.getId() != null ? new OracleId(entity.getId()) : null, // Long → OracleId
                new Question(entity.getQuestion()),                          // String → Question VO
                Lines.fromJson(entity.getLinesJson()),                       // String(JSON) → Lines VO
                entity.getOriginalHexagram() != null ? entity.getOriginalHexagram().getLinesBits() : null,
                entity.getChangedHexagram() != null ? entity.getChangedHexagram().getLinesBits() : null,
                null, // CenterLineResult (Entity에 없으니 보류)
                entity.getCreatedAt(),
                entity.getLocale(),
                entity.getUser() != null ? entity.getUser().getId() : null   // UserEntity → userId(Long)
        );
    }

}
