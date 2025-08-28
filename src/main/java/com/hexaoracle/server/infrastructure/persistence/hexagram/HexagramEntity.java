package com.hexaoracle.server.infrastructure.persistence.hexagram;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "hexagram",
        indexes = {
                @Index(name = "idx_hexagram_binary", columnList = "binary")
        }
)
@Getter
@NoArgsConstructor
public class HexagramEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;   // 1 ~ 64

    @Column(name = "name_zh", length = 8, nullable = false)
    private String nameZh;

    @Column(name = "name_ko", length = 16, nullable = false)
    private String nameKo;

    @Column(name = "name_en", length = 32, nullable = false)
    private String nameEn;

    @Column(name = "binary", length = 6, nullable = false)
    private String binary;  // "111000" 형식, 하→상

    @Column(name = "upper_trigram", length = 4, nullable = false)
    private String upperTrigram;

    @Column(name = "lower_trigram", length = 4, nullable = false)
    private String lowerTrigram;

    @Lob
    @Column(name = "judgement")
    private String judgement;

    @Lob
    @Column(name = "image")
    private String image;
}
