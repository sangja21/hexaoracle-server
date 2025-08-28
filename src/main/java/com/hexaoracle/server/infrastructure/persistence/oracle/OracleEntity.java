package com.hexaoracle.server.infrastructure.persistence.oracle;

import com.hexaoracle.server.infrastructure.persistence.hexagram.HexagramEntity;
import com.hexaoracle.server.infrastructure.persistence.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oracle",
        indexes = {
                @Index(name = "idx_oracle_owner_created_id", columnList = "user_id, created_at DESC, id DESC"),
                @Index(name = "idx_oracle_original", columnList = "original_hexagram_id"),
                @Index(name = "idx_oracle_changed", columnList = "changed_hexagram_id"),
                @Index(name = "idx_oracle_bits", columnList = "lines_bits")
        }
)
@Getter
@NoArgsConstructor
public class OracleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User (nullable → 게스트 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Lob
    @Column(name = "question", nullable = false)
    private String question;

    // 원본 효 배열 (JSON 저장)
    @Column(name = "lines_json", columnDefinition = "json", nullable = false)
    private String linesJson;

    // 계산된 비트 표현 (DB generated column, JPA는 읽기 전용)
    @Column(name = "lines_bits", length = 6, insertable = false, updatable = false)
    private String linesBits;

    // 변효 개수 (DB generated column)
    @Column(name = "moving_count", insertable = false, updatable = false)
    private Integer movingCount;

    // 본괘 / 변괘
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_hexagram_id", nullable = false)
    private HexagramEntity originalHexagram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_hexagram_id")
    private HexagramEntity changedHexagram;

    @Column(name = "locale", length = 10, nullable = false)
    private String locale;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
