package com.hexaoracle.server.domain.oracle.model;

import com.hexaoracle.server.domain.hexagram.model.Lines;
import com.hexaoracle.server.domain.oracle.service.CenterLineService;
import com.hexaoracle.server.domain.oracle.service.ChangedHexagramService;
import com.hexaoracle.server.domain.oracle.service.HexagramService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OracleFactory {

    private final HexagramService hexagramService = new HexagramService();
    private final ChangedHexagramService changedHexagramService = new ChangedHexagramService();
    private final CenterLineService centerLineService = new CenterLineService();

    public Oracle create(Question question, Lines lines, String locale, Long userId) {
        if (question == null || lines == null) {
            throw new IllegalArgumentException("Question and Lines must not be null");
        }

        // (1) 본괘 binary
        String originalBinary = hexagramService.toBinary(lines);

        // (2) 지괘 binary
        String changedBinary = changedHexagramService.computeChangedBinary(lines);

        // (3) 중심효 계산
        CenterLineResult centerLineResult = centerLineService.extract(lines);

        // (4) Oracle Aggregate 조립
        return new Oracle(
                new OracleId(System.currentTimeMillis()),
                question,
                lines,
                originalBinary,        // 본괘 binary
                changedBinary,         // 지괘 binary (없으면 null)
                centerLineResult,      // 중심효/중심괘 정보
                LocalDateTime.now(),
                locale,
                userId
        );
    }
}
