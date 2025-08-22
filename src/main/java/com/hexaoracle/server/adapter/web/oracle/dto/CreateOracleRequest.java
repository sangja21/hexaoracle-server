package com.hexaoracle.server.adapter.web.oracle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOracleRequest {

    @NotBlank(message = "질문은 필수 입력입니다.")
    private String question;

    @NotNull(message = "lines 배열은 필수입니다.")
    @Size(min = 6, max = 6, message = "lines는 반드시 6개의 요소여야 합니다.")
    private List<Integer> lines;

    @NotBlank(message = "locale은 필수 입력입니다.")
    private String locale;
}
