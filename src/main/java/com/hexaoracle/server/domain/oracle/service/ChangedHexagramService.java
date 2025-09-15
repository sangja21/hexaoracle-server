package com.hexaoracle.server.domain.oracle.service;

import com.hexaoracle.server.domain.hexagram.model.Lines;

import java.util.ArrayList;
import java.util.List;

public class ChangedHexagramService {

    /**
     * Lines → 변괘 binary (없으면 null)
     */
    public String computeChangedBinary(Lines lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines cannot be null");
        }

        List<Integer> flipped = new ArrayList<>();
        boolean hasMoving = false;

        for (int i = 0; i < 6; i++) {
            int val = lines.get(i).value();
            if (val == 6) { // 노음 → 양
                flipped.add(7);
                hasMoving = true;
            } else if (val == 9) { // 노양 → 음
                flipped.add(8);
                hasMoving = true;
            } else {
                flipped.add(val);
            }
        }

        if (!hasMoving) {
            return null; // 변효 없으면 지괘 없음
        }

        // flip된 Lines → binary string
        StringBuilder sb = new StringBuilder();
        for (int v : flipped) {
            sb.append((v == 7 || v == 9) ? "1" : "0");
        }
        return sb.toString();
    }
}
