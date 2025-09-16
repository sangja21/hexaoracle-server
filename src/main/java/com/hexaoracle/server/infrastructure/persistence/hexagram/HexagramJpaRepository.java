package com.hexaoracle.server.infrastructure.persistence.hexagram;
import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface HexagramJpaRepository extends JpaRepository<HexagramEntity, Long> {

    @Query(value = """
        SELECT * 
        FROM hexagram h
        WHERE (:q IS NULL OR
              (CASE 
                  WHEN :locale = 'ko' THEN h.name_ko
                  WHEN :locale = 'en' THEN h.name_en
                  ELSE h.name_zh
               END) LIKE %:q%)
          AND (:cursor IS NULL OR h.id > :cursor)
        ORDER BY h.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<HexagramEntity> findByCriteria(
            @Param("q") String q,
            @Param("locale") String locale,
            @Param("cursor") Integer cursor,
            @Param("limit") int limit
    );
}

