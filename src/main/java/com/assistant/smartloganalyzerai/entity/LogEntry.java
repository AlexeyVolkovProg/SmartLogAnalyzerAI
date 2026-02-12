package com.assistant.smartloganalyzerai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Builder
@Entity
@Setter
@Getter
@Table(name = "log_entries", schema = "log_common")
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "log_level", nullable = false, length = 10)
    private String logLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Array(length = 1024)
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;
}