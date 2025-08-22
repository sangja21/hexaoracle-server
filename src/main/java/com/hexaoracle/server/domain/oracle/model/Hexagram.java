package com.hexaoracle.server.domain.oracle.model;

public class Hexagram {
    private final Lines lines;
    private final Trigram upper;
    private final Trigram lower;
    private final int kingWenNo;

    public Hexagram(Lines lines, Trigram upper, Trigram lower, int kingWenNo) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines cannot be null");
        }
        this.lines = lines;
        this.upper = upper;
        this.lower = lower;
        this.kingWenNo = kingWenNo;
    }

    public Lines getLines() {
        return lines;
    }

    public Trigram getUpper() {
        return upper;
    }

    public Trigram getLower() {
        return lower;
    }

    public int getKingWenNo() {
        return kingWenNo;
    }
}
