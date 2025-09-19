package com.hexaoracle.server.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexaoracle.server.adapter.web.hexagram.HexagramController;
import com.hexaoracle.server.application.hexagram.GetHexagramUseCase;
import com.hexaoracle.server.application.hexagram.ListHexagramsUseCase;
import com.hexaoracle.server.application.hexagram.dto.HexagramDetailDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import com.hexaoracle.server.application.hexagram.dto.HexagramLineDto;
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

    @MockitoBean
    private ListHexagramsUseCase listHexagramsUseCase;

    @MockitoBean
    private GetHexagramUseCase getHexagramUseCase;

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

    @Test
    @DisplayName("GET /api/v1/hexagrams/{no} → 단일 괘 상세 응답 확인")
    void getHexagramById() throws Exception {
        // given
        short hexagramNo = 23;

        List<HexagramLineDto> mockLines = List.of(
                new HexagramLineDto((short)1, "초육 효사(한글)", "Line 1 EN", "Line 1 ZH"),
                new HexagramLineDto((short)2, "구이 효사(한글)", "Line 2 EN", "Line 2 ZH")
                // ... 필요한 만큼 6개 다 넣어도 되고, 최소 1~2개만 테스트해도 됨
        );

        HexagramDetailDto mockDto = new HexagramDetailDto(
                hexagramNo,
                "박",
                "Bo",
                "剝",
                "000001",
                "☶",
                "☷",
                "박괘의 판단",
                "박괘의 이미지",
                mockLines
        );

        when(getHexagramUseCase.execute(hexagramNo, "ko"))
                .thenReturn(mockDto);

        // when & then
        mockMvc.perform(get("/api/v1/hexagrams/{no}", hexagramNo)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(23))
                .andExpect(jsonPath("$.data.nameKo").value("박"))
                .andExpect(jsonPath("$.data.nameEn").value("Bo"))
                .andExpect(jsonPath("$.data.nameZh").value("剝"))
                .andExpect(jsonPath("$.data.linesBits").value("000001"))
                .andExpect(jsonPath("$.data.upperTrigram").value("☶"))
                .andExpect(jsonPath("$.data.lowerTrigram").value("☷"))
                .andExpect(jsonPath("$.data.judgement").value("박괘의 판단"))
                .andExpect(jsonPath("$.data.image").value("박괘의 이미지"))
                .andExpect(jsonPath("$.data.lines[0].lineNo").value(1))
                .andExpect(jsonPath("$.data.lines[0].textKo").value("초육 효사(한글)"));
    }

}
