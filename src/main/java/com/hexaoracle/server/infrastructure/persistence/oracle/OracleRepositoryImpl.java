package com.hexaoracle.server.infrastructure.persistence.oracle;

import com.hexaoracle.server.domain.oracle.model.Oracle;
import com.hexaoracle.server.domain.oracle.port.OracleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OracleRepositoryImpl implements OracleRepository {

    private final OracleJpaRepository jpaRepository;
    private final OracleMapper mapper; // Entity ↔ Domain 변환 책임

    public OracleRepositoryImpl(OracleJpaRepository jpaRepository, OracleMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Oracle save(Oracle oracle) {
        OracleEntity entity = mapper.toEntity(oracle);
        OracleEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Oracle> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}
