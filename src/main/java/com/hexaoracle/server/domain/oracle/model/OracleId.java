package com.hexaoracle.server.domain.oracle.model;

import java.util.UUID;

public record OracleId(UUID value) {
    public OracleId {
        if (value == null) throw new IllegalArgumentException("OracleId cannot be null");
    }
}
