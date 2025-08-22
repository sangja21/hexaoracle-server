package com.hexaoracle.server.domain.oracle;

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
    @DisplayName("POST /oracles 정상 입력 시 점괘가 생성된다")
    void createOracle_success() throws Exception {
        String requestBody = """
        {
          "question": "다음 분기 제품 출시 시기를 정해도 될까?",
          "lines": [7,8,9,7,8,8],
          "locale": "ko-KR"
        }
        """;

        mockMvc.perform(
                        post("/oracles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dummy-token")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.oracleId").exists())
                .andExpect(jsonPath("$.chosen.lines").isArray())
                .andExpect(jsonPath("$.chosen.lines.length()").value(6))
                .andExpect(jsonPath("$.interpretation").exists());
    }
}

