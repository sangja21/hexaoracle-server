package com.hexaoracle.server.application.hexagram.dto;

public record HexagramDto(
        Short id,
        String nameKo,
        String nameEn,
        String nameZh,
        String linesBits,
        String upperTrigram,
        String lowerTrigram,
        String judgement,
        String image
) {}

