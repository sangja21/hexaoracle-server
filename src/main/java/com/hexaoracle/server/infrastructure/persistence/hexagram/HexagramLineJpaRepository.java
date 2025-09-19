package com.hexaoracle.server.infrastructure.persistence.hexagram;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Line 전용 레포지토리
public interface HexagramLineJpaRepository extends JpaRepository<HexagramLineEntity, Long> {
    List<HexagramLineEntity> findByHexagramId(Short hexagramId);
}

