package com.hexaoracle.server.adapter.web.oracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateOracleResponse {
    private String oracleId;
    private String question;
    private String hexagramName;
    private int hexagramNo;
}
