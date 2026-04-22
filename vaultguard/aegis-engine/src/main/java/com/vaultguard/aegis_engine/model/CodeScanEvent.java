package com.vaultguard.aegis_engine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_scan_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeScanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String scanId;

    @Column(nullable = false)
    private String developerId;

    // SAFE, SENSITIVE, BLOCKED
    @Column(nullable = false)
    private String result;

    // API_KEY, PASSWORD, DB_SCHEMA, PII, CLEAN
    @Column(nullable = false)
    private String sensitivityType;

    @Column(length = 2000)
    private String originalCode;

    @Column(length = 2000)
    private String redactedCode;

    @Column(nullable = false)
    private String action;

    private Double riskScore;

    @Column(nullable = false)
    private LocalDateTime scannedAt;
}