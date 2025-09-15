package com.hexaoracle.server.domain.hexagram.model;

import java.util.Objects;

/**
 * Hexagram (괘)
 * - 6개의 효(lines)로부터 파생된 상괘/하괘 + King Wen 번호를 가진 값 객체
 * - 불변(Immutable) 보장
 */
public final class Hexagram {

    private final Lines lines;     // 반드시 6효 길이 보장
    private final Trigram upper;   // lines[4..6]에서 파생
    private final Trigram lower;   // lines[1..3]에서 파생
    private final int kingWenNo;   // 1..64 범위

    private Hexagram(Lines lines, Trigram upper, Trigram lower, int kingWenNo) {
        this.lines = Objects.requireNonNull(lines, "lines cannot be null");
        this.upper = Objects.requireNonNull(upper, "upper cannot be null");
        this.lower = Objects.requireNonNull(lower, "lower cannot be null");

        if (kingWenNo < 1 || kingWenNo > 64) {
            throw new IllegalArgumentException("kingWenNo must be between 1 and 64");
        }
        this.kingWenNo = kingWenNo;
    }

    /**
     * 정적 팩토리 메서드
     * - lines로부터 upper/lower를 계산하고, 매핑 테이블에서 kingWenNo를 조회
     */
    public static Hexagram of(Lines lines) {
        Objects.requireNonNull(lines, "lines cannot be null");
        if (lines.size() != 6) {
            throw new IllegalArgumentException("Hexagram must have exactly 6 lines");
        }

        Trigram lower = Trigram.from(lines.subLines(0, 3)); // 1~3효
        Trigram upper = Trigram.from(lines.subLines(3, 6)); // 4~6효
        int kingWenNo = HexagramMapping.lookup(lines); // ✅ binary 기반 조회

        return new Hexagram(lines, upper, lower, kingWenNo);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hexagram other)) return false;
        return kingWenNo == other.kingWenNo && lines.equals(other.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, kingWenNo);
    }

    @Override
    public String toString() {
        return "Hexagram{" +
                "lines=" + lines +
                ", upper=" + upper +
                ", lower=" + lower +
                ", kingWenNo=" + kingWenNo +
                '}';
    }
}
