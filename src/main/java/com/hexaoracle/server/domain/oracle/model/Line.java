package com.hexaoracle.server.domain.oracle.model;

public record Line(int value) {
    public Line {
        if (value < 6 || value > 9) {
            throw new IllegalArgumentException("Line must be 6,7,8,9");
        }
    }

    public boolean isYang() {
        return value == 7 || value == 9;
    }

    public boolean isMoving() {
        return value == 6 || value == 9;
    }

    public Line toChanged() {
        return switch (value) {
            case 6 -> new Line(7);
            case 9 -> new Line(8);
            default -> this;
        };
    }
}
