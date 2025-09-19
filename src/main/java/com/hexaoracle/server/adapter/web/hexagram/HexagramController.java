package com.hexaoracle.server.adapter.web.hexagram;

import com.hexaoracle.server.api.common.dto.ApiResponse;
import com.hexaoracle.server.api.common.dto.PageResponseDTO;
import com.hexaoracle.server.application.hexagram.GetHexagramUseCase;
import com.hexaoracle.server.application.hexagram.ListHexagramsUseCase;
import com.hexaoracle.server.application.hexagram.dto.HexagramDetailDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hexagrams")
@RequiredArgsConstructor
public class HexagramController {

    private final ListHexagramsUseCase listHexagramsUseCase;
    private final GetHexagramUseCase getHexagramUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<HexagramDto>>> getHexagrams(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ko") String locale,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PageResponseDTO<HexagramDto> page = listHexagramsUseCase.execute(q, locale, cursor, limit);

        return ApiResponse.success(HttpStatus.OK.value(), page);

    }

    @GetMapping("/{no}")
    public ResponseEntity<ApiResponse<HexagramDetailDto>> getHexagramByNo(
            @PathVariable short no,
            @RequestParam(defaultValue = "ko") String locale
    ) {
        HexagramDetailDto hexagram = getHexagramUseCase.execute(no, locale);
        return ApiResponse.success(HttpStatus.OK.value(), hexagram);
    }

} // HexagramController


