package com.hexaoracle.server.domain.oracle.model;

import java.time.LocalDateTime;

public class Oracle {
    private final OracleId id;
    private final Question question;
    private final Lines lines;
    private final String originalBinary;
    private final String changedBinary; // nullable
    private final CenterLineResult centerLineResult;
    private final LocalDateTime createdAt;
    private final String locale;
    private final Long userId; // nullable

    public Oracle(OracleId id,
                  Question question,
                  Lines lines,
                  String originalBinary,
                  String changedBinary,
                  CenterLineResult centerLineResult,
                  LocalDateTime createdAt,
                  String locale,
                  Long userId) {
        this.id = id;
        this.question = question;
        this.lines = lines;
        this.originalBinary = originalBinary;
        this.changedBinary = changedBinary;
        this.centerLineResult = centerLineResult;
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

    public String getOriginalBinary() {
        return originalBinary;
    }

    public String getChangedBinary() {
        return changedBinary;
    }

    public CenterLineResult getCenterLineResult() {
        return centerLineResult;
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
