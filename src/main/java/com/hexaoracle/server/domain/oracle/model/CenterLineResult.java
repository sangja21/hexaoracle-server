package com.hexaoracle.server.domain.oracle.model;

import java.util.Set;

public record CenterLineResult(
        CenterHexagram centerHexagram,
        Set<Integer> changeLineSet,
        int centerLine
) {}
