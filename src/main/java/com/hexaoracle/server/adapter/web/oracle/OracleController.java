package com.hexaoracle.server.adapter.web.oracle;

import com.hexaoracle.server.api.common.dto.ApiResponse;
import com.hexaoracle.server.application.oracle.CreateOracleUseCase;
import com.hexaoracle.server.domain.hexagram.model.Line;
import com.hexaoracle.server.domain.oracle.model.Oracle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oracles")
public class OracleController {

    private final CreateOracleUseCase createOracleUseCase;

    public OracleController(CreateOracleUseCase createOracleUseCase) {
        this.createOracleUseCase = createOracleUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OracleResponse>> createOracle(@RequestBody OracleRequest request) {
        Oracle oracle = createOracleUseCase.execute(
                request.lines(),
                request.question(),
                request.locale(),
                request.userId()
        );

        OracleResponse response = OracleResponse.from(oracle);

        // ApiResponse.success()로 감싸서 반환
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(response));
    }

    // --- Request DTO ---
    public record OracleRequest(
            List<Integer> lines,
            String question,
            String locale,
            Long userId
    ) {}

    // --- Response DTO ---
    public record OracleResponse(
            String oracleId,
            String question,
            List<Integer> lines,
            String originalBinary,
            String changedBinary,
            int centerLine,
            String locale,
            Long userId,
            String createdAt
    ) {
        static OracleResponse from(Oracle oracle) {
            return new OracleResponse(
                    oracle.getId().value().toString(),
                    oracle.getQuestion().text(),
                    oracle.getLines().values().stream().map(Line::value).toList(),
                    oracle.getOriginalBinary(),
                    oracle.getChangedBinary(),
                    oracle.getCenterLineResult().centerLine(),
                    oracle.getLocale(),
                    oracle.getUserId(),
                    oracle.getCreatedAt().toString()
            );
        }
    }
}
