package com.hexaoracle.server.domain.oracle.port;

import com.hexaoracle.server.domain.oracle.model.Oracle;

import java.util.Optional;

public interface OracleRepository {

    /**
     * 새로운 Oracle 저장
     */
    Oracle save(Oracle oracle);

    /**
     * Oracle 식별자로 조회
     */
    Optional<Oracle> findById(Long id);

    /**
     * 사용자별 Oracle 기록 페이지네이션 조회
     * (추후 MyPage/History 유즈케이스에서 활용)
     */
    // Page<Oracle> findByUserId(Long userId, Pageable pageable);
}
