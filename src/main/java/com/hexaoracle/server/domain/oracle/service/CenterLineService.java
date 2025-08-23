package com.hexaoracle.server.domain.oracle.service;

import com.hexaoracle.server.domain.oracle.model.CenterHexagram;
import com.hexaoracle.server.domain.oracle.model.CenterLineResult;
import com.hexaoracle.server.domain.oracle.model.Lines;

import java.util.Set;
import java.util.TreeSet;

public class CenterLineService {

    /**
     * Lines → 중심효 계산
     */
    public CenterLineResult extract(Lines lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines cannot be null");
        }

        // (1) 변효(6,9) 집합 추출 (1~6 index, 하→상)
        Set<Integer> changeLineSet = new TreeSet<>();
        for (int i = 0; i < 6; i++) {
            int val = lines.get(i).value();
            if (val == 6 || val == 9) {
                changeLineSet.add(i + 1); // 1~6
            }
        }

        int count = changeLineSet.size();
        int centerLine = 0;
        CenterHexagram centerHexagram = CenterHexagram.ORIGINAL;

        // (2) 조건 분기
        if (count == 0) {
            // i. 변효 없음
            centerLine = 0;
            centerHexagram = CenterHexagram.ORIGINAL;

        } else if (count == 1) {
            // ii. 유일한 원소
            centerLine = changeLineSet.iterator().next();
            centerHexagram = CenterHexagram.ORIGINAL;

        } else if (count == 2) {
            // iii. 가장 작은 숫자
            centerLine = changeLineSet.iterator().next();
            centerHexagram = CenterHexagram.ORIGINAL;

        } else if (count == 3) {
            // iv. 가운데 숫자
            int mid = changeLineSet.stream().skip(1).findFirst().get();
            centerLine = mid;
            centerHexagram = CenterHexagram.ORIGINAL;

        } else if (count == 4) {
            // v. 가장 큰 숫자
            centerLine = changeLineSet.stream().reduce((first, second) -> second).get();
            centerHexagram = CenterHexagram.CHANGED;

        } else if (count == 5) {
            // vi. 가운데 숫자
            int mid = changeLineSet.stream().skip(2).findFirst().get();
            centerLine = mid;
            centerHexagram = CenterHexagram.CHANGED;

        } else if (count == 6) {
            // vii. 가장 큰 숫자
            centerLine = changeLineSet.stream().reduce((first, second) -> second).get();
            centerHexagram = CenterHexagram.CHANGED;
        }

        return new CenterLineResult(centerHexagram, changeLineSet, centerLine);
    }
}
