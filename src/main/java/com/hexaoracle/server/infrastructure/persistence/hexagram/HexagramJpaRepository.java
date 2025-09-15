package com.hexaoracle.server.infrastructure.persistence.hexagram;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HexagramJpaRepository extends JpaRepository<HexagramEntity, Short> {
}


