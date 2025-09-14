package com.hexaoracle.server.adapter.web.hexagram;

import com.hexaoracle.server.api.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hexagrams")
public class HexagramController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Integer>>>> getAll() {
        Map<String, List<Integer>> body = Map.of("items", List.of(7, 8, 9, 6, 7, 8));
        return ResponseEntity.ok(ApiResponse.success(body));
    }
}



