package com.hexaoracle.server.domain.oracle.model;

import java.time.LocalDateTime;
import java.util.Set;

public class Oracle {
    private final OracleId id;
    private final Question question;
    private final Lines lines;
    private final Hexagram originalHexagram;
    private final Hexagram changedHexagram; // nullable
    private final Set<Integer> movingLineIndexes;
    private final LocalDateTime createdAt;
    private final String locale;
    private final Long userId; // nullable

    public Oracle(OracleId id,
                  Question question,
                  Lines lines,
                  Hexagram originalHexagram,
                  Hexagram changedHexagram,
                  Set<Integer> movingLineIndexes,
                  LocalDateTime createdAt,
                  String locale,
                  Long userId) {
        this.id = id;
        this.question = question;
        this.lines = lines;
        this.originalHexagram = originalHexagram;
        this.changedHexagram = changedHexagram;
        this.movingLineIndexes = movingLineIndexes;
        this.createdAt = createdAt;
        this.locale = locale;
        this.userId = userId;
    }

    public OracleId getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public Lines getLines() {
        return lines;
    }

    public Hexagram getOriginalHexagram() {
        return originalHexagram;
    }

    public Hexagram getChangedHexagram() {
        return changedHexagram;
    }

    public Set<Integer> getMovingLineIndexes() {
        return movingLineIndexes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getLocale() {
        return locale;
    }

    public Long getUserId() {
        return userId;
    }
}
