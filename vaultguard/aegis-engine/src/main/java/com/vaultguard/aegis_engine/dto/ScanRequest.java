package com.vaultguard.aegis_engine.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    private String developerId;
    private String code;
    private String context;
}