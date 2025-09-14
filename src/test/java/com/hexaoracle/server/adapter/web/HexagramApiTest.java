package com.hexaoracle.server.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexaoracle.server.adapter.web.hexagram.HexagramController;
import com.hexaoracle.server.domain.oracle.service.HexagramService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org. springframework. test. context. bean. override. mockito. MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Web Adapter 계층만 테스트 (HexagramController)
@WebMvcTest(HexagramController.class)
class HexagramApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 실제 서비스 대신 Mock 주입
    @MockitoBean
    private HexagramService hexagramService;

    @Test
    @DisplayName("GET /api/v1/hexagrams → 목록 응답 구조 확인")
    void getAllHexagrams_minimal() throws Exception {
        mockMvc.perform(get("/api/v1/hexagrams")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items").isArray());
    }




}
