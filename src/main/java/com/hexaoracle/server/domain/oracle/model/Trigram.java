package com.hexaoracle.server.domain.oracle.model;

import java.util.List;
import java.util.Map;

public enum Trigram {
    QIAN("☰", "111"), // 건 (하늘)
    KUN("☷", "000"), // 곤 (땅)
    KAN("☵", "010"), // 감 (물)
    LI("☲", "101"),  // 리 (불)
    ZHEN("☳", "100"),// 진 (우레)
    XUN("☴", "110"), // 손 (바람/나무)
    GEN("☶", "001"), // 간 (산)
    DUI("☱", "011"); // 태 (못)

    private final String symbol;
    private final String binary; // 하→상 순서, 1=양, 0=음

    Trigram(String symbol, String binary) {
        this.symbol = symbol;
        this.binary = binary;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBinary() {
        return binary;
    }

    // Lines(3개)로부터 Trigram 도출
    public static Trigram from(List<Line> lines) {
        if (lines.size() != 3) {
            throw new IllegalArgumentException("Trigram must have exactly 3 lines");
        }
        // 하효→상효 순서로 binary 생성
        StringBuilder sb = new StringBuilder();
        for (Line line : lines) {
            sb.append(line.isYang() ? "1" : "0");
        }
        String key = sb.toString();

        for (Trigram t : values()) {
            if (t.binary.equals(key)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown trigram binary: " + key);
    }

    private static final Map<String, Trigram> BINARY_MAP =
            Map.ofEntries(
                    Map.entry("111", QIAN),
                    Map.entry("000", KUN),
                    Map.entry("010", KAN),
                    Map.entry("101", LI),
                    Map.entry("100", ZHEN),
                    Map.entry("110", XUN),
                    Map.entry("001", GEN),
                    Map.entry("011", DUI)
            );

    public static Trigram fromBinary(String binary) {
        Trigram t = BINARY_MAP.get(binary);
        if (t == null) {
            throw new IllegalArgumentException("Invalid trigram binary: " + binary);
        }
        return t;
    }
}
