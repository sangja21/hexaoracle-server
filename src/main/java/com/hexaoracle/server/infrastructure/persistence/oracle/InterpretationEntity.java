package com.hexaoracle.server.infrastructure.persistence.oracle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interpretation",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_interp_oracle", columnNames = {"oracle_id"})
        },
        indexes = {
                @Index(name = "idx_interp_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor
public class InterpretationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Oracle과 1:1 연결
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oracle_id", nullable = false, unique = true)
    private OracleEntity oracle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @Lob
    @Column(name = "ai_commentary")
    private String aiCommentary;

    @Column(name = "version", length = 16, nullable = false)
    private String version;

    @Column(name = "locale", length = 10, nullable = false)
    private String locale;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
