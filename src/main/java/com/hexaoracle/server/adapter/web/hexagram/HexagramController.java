package com.hexaoracle.server.adapter.web.hexagram;

import com.hexaoracle.server.api.common.dto.ApiResponse;
import com.hexaoracle.server.application.hexagram.ListHexagramsUseCase;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hexagrams")
public class HexagramController {

    private final ListHexagramsUseCase listHexagramsUseCase;

    public HexagramController(ListHexagramsUseCase listHexagramsUseCase) {
        this.listHexagramsUseCase = listHexagramsUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll() {
        List<HexagramDto> items = listHexagramsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(Map.of("items", items)));
    }
}
