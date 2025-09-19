package com.hexaoracle.server.application.hexagram.dto;

import java.util.List;

public record HexagramDetailDto(
        Short id,
        String nameKo,
        String nameEn,
        String nameZh,
        String linesBits,
        String upperTrigram,
        String lowerTrigram,
        String judgement,
        String image,
        List<HexagramLineDto> lines
) {}
