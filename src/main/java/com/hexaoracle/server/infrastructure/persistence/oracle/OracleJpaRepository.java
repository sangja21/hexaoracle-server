package com.hexaoracle.server.infrastructure.persistence.oracle;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OracleJpaRepository extends JpaRepository<OracleEntity, Long> {
    // 필요 시 커스텀 쿼리 추가
}
