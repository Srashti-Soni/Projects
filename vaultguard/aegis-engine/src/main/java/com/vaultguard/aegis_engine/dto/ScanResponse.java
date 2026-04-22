package com.vaultguard.aegis_engine.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private String scanId;
    private String result;
    private String action;
    private String sensitivityType;
    private Double riskScore;
    private String redactedCode;
    private List<String> detectedPatterns;
    private String message;
}