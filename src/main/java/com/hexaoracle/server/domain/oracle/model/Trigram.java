package com.hexaoracle.server.domain.oracle.model;

public enum Trigram {
    QIAN("☰"), KUN("☷"), KAN("☵"), LI("☲"),
    ZHEN("☳"), XUN("☴"), GEN("☶"), DUI("☱");

    private final String symbol;
    Trigram(String symbol) { this.symbol = symbol; }
    public String getSymbol() { return symbol; }
}
