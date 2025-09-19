package com.hexaoracle.server.domain.oracle.service;

import com.hexaoracle.server.domain.hexagram.model.Lines;

public class HexagramService {

    /**
     * Lines → 본괘 binary (6자리, 하효→상효)
     */
    public String toBinary(Lines lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines cannot be null");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int val = lines.get(i).value();
            sb.append((val == 7 || val == 9) ? "1" : "0");
        }
        return sb.toString();
    }
}
