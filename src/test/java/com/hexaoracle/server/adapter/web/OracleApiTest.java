package com.hexaoracle.server.adapter.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OracleApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/oracles 정상 입력 시 점괘가 생성된다")
    void createOracle_success() throws Exception {
        String requestBody = """
        {
          "question": "다음 분기 제품 출시 시기를 정해도 될까?",
          "lines": [7,8,9,7,8,8],
          "locale": "ko-KR"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/oracles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dummy-token")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.oracleId").exists())
                .andExpect(jsonPath("$.data.lines").isArray())
                .andExpect(jsonPath("$.data.lines.length()").value(6))
                .andExpect(jsonPath("$.data.originalBinary").exists())
                .andExpect(jsonPath("$.data.changedBinary").exists())
                .andExpect(jsonPath("$.data.centerLine").exists())
                .andExpect(jsonPath("$.data.locale").value("ko-KR"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/oracles 실패 - lines 길이가 6이 아님")
    void createOracle_invalidLinesLength() throws Exception {
        String requestBody = """
    {
      "question": "테스트 질문",
      "lines": [7,8,9,7],
      "locale": "ko-KR"
    }
    """;

        mockMvc.perform(post("/api/v1/oracles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer dummy-token")
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_LINES"));
    }

    @Test
    @DisplayName("POST /api/v1/oracles 실패 - lines 값이 범위를 벗어남")
    void createOracle_invalidLineValue() throws Exception {
        String requestBody = """
    {
      "question": "테스트 질문",
      "lines": [7,8,9,7,5,8],
      "locale": "ko-KR"
    }
    """;

        mockMvc.perform(post("/api/v1/oracles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer dummy-token")
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_LINES"));
    }


}
