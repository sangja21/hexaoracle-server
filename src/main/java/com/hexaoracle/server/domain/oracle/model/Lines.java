package com.hexaoracle.server.domain.oracle.model;

import java.util.List;

public record Lines(List<Line> values) {
    public Lines {
        if (values == null || values.size() != 6) {
            throw new IllegalArgumentException("Lines must contain 6 Line values");
        }
    }

    public Line get(int index) {
        return values.get(index);
    }
}
