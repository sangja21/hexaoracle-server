package com.hexaoracle.server.infrastructure.persistence.hexagram;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "hexagram_line",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_hexagram_line", columnNames = {"hexagram_id", "line_index"})
        }
)
@Getter
@NoArgsConstructor
public class HexagramLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hexagram_id", nullable = false)
    private HexagramEntity hexagram;

    @Column(name = "line_index", nullable = false)
    private Byte lineIndex; // 1..6

    @Lob
    @Column(name = "text_zh")
    private String textZh;

    @Lob
    @Column(name = "text_ko")
    private String textKo;

    @Lob
    @Column(name = "text_en")
    private String textEn;
}
