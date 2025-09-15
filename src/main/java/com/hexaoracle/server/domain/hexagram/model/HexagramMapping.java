package com.hexaoracle.server.domain.hexagram.model;

import java.util.Map;

// 불변하는 데이터인 괘의 정보들을 메모리맵에 저장하여 DB호출 없이 번호를 반환한다.
public final class HexagramMapping {

    // binary(하효→상효, 1=양, 0=음) → King Wen 번호
    private static final Map<String, Integer> BINARY_TO_NO = Map.ofEntries(
            Map.entry("111111", 1),  // 건위천
            Map.entry("000000", 2),  // 곤위지
            Map.entry("100010", 3),  // 수뢰둔
            Map.entry("010001", 4),  // 산수몽
            Map.entry("111010", 5),  // 수천수
            Map.entry("010111", 6),  // 천수송
            Map.entry("010000", 7),  // 지수사
            Map.entry("000010", 8),  // 수지비
            Map.entry("111011", 9),  // 풍천소축
            Map.entry("110111", 10), // 천택리
            Map.entry("111000", 11), // 지천태
            Map.entry("000111", 12), // 천지비
            Map.entry("101111", 13), // 천화동인
            Map.entry("111101", 14), // 화천대유
            Map.entry("001000", 15), // 지산겸
            Map.entry("000100", 16), // 뇌지예
            Map.entry("100110", 17), // 택뢰수
            Map.entry("011001", 18), // 산풍고
            Map.entry("110000", 19), // 지택림
            Map.entry("000011", 20), // 풍지관
            Map.entry("100101", 21), // 화뢰서합
            Map.entry("101001", 22), // 산화비
            Map.entry("000001", 23), // 산지박
            Map.entry("100000", 24), // 지뢰복
            Map.entry("100111", 25), // 천뢰무망
            Map.entry("111001", 26), // 산천대축
            Map.entry("100001", 27), // 산뢰이
            Map.entry("011110", 28), // 택풍대과
            Map.entry("010010", 29), // 감위수
            Map.entry("101101", 30), // 이위화
            Map.entry("001110", 31), // 택산함
            Map.entry("011100", 32), // 뇌풍항
            Map.entry("001111", 33), // 천산돈
            Map.entry("111100", 34), // 뇌천대장
            Map.entry("000101", 35), // 화지진
            Map.entry("101000", 36), // 지화명이
            Map.entry("101011", 37), // 풍화가인
            Map.entry("110101", 38), // 화택규
            Map.entry("001010", 39), // 수산건
            Map.entry("010100", 40), // 뇌수해
            Map.entry("110001", 41), // 산택손
            Map.entry("100011", 42), // 풍뢰익
            Map.entry("111110", 43), // 택천쾌
            Map.entry("011111", 44), // 천풍구
            Map.entry("000110", 45), // 택지췌
            Map.entry("011000", 46), // 지풍승
            Map.entry("010110", 47), // 택수곤
            Map.entry("011010", 48), // 수풍정
            Map.entry("101110", 49), // 택화혁
            Map.entry("011101", 50), // 화풍정
            Map.entry("100100", 51), // 진위뢰
            Map.entry("001001", 52), // 간위산
            Map.entry("001011", 53), // 풍산점
            Map.entry("110100", 54), // 뇌택귀매
            Map.entry("101100", 55), // 뇌화풍
            Map.entry("001101", 56), // 화산려
            Map.entry("011011", 57), // 손위풍
            Map.entry("110110", 58), // 태위택
            Map.entry("010011", 59), // 풍수환
            Map.entry("110010", 60), // 수택절
            Map.entry("110011", 61), // 풍택중부
            Map.entry("001100", 62), // 뇌산소과
            Map.entry("101010", 63), // 수화기제
            Map.entry("010101", 64)  // 화수미제

    );

    private HexagramMapping() {
        // 인스턴스화 방지
    }

    /** Lines → King Wen No */
    public static int lookup(Lines lines) {
        String binary = lines.toBinary(); // ex: "010111"
        Integer no = BINARY_TO_NO.get(binary);
        if (no == null) {
            throw new IllegalArgumentException("Invalid hexagram binary: " + binary);
        }
        return no;
    }
}
