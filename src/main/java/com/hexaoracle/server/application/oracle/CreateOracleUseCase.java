package com.hexaoracle.server.application.oracle;

import com.hexaoracle.server.domain.oracle.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service   // 스프링 빈 등록
public class CreateOracleUseCase {

    private final OracleFactory oracleFactory;

    public CreateOracleUseCase(OracleFactory oracleFactory) {
        this.oracleFactory = oracleFactory;
    }

    /**
     * 새로운 Oracle(점괘) 생성
     *
     * @param lineValues 6개의 효 값 (6,7,8,9)
     * @param question   사용자가 입력한 질문
     * @param locale     언어 (예: "ko-KR", "en-US")
     * @param userId     사용자 ID
     * @return 생성된 Oracle Aggregate
     */
    public Oracle execute(List<Integer> lineValues, String question, String locale, Long userId) {
        if (lineValues == null || lineValues.size() != 6) {
            throw new IllegalArgumentException("Exactly 6 line values are required");
        }

        // 1. Question VO 생성
        Question questionObj = new Question(question);

        // 2. Lines VO 생성
        Lines lines = new Lines(lineValues.stream().map(Line::new).toList());

        // 3. OracleFactory 호출 (도메인 규칙 적용)
        Oracle oracle = oracleFactory.create(questionObj, lines, locale, userId);

        // 4. 저장 (Repository는 나중에 연결)
        // TODO: OracleRepository.save(oracle);

        // 5. Aggregate 반환
        return oracle;
    }
}
