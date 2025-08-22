package com.hexaoracle.server.domain.oracle.model;

public record Question(String text) {
    public Question {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        if (text.length() > 500) {
            throw new IllegalArgumentException("Question too long");
        }
        // TODO: 금칙어/PII 필터
    }
}
