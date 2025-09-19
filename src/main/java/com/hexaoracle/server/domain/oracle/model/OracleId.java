package com.hexaoracle.server.domain.oracle.model;

public record OracleId(Long value) {
    public OracleId {
        if (value == null) throw new IllegalArgumentException("OracleId cannot be null");
    }

    public Long getValue() {
        return value;
    }
}
