package com.vaultguard.fintech_engine.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictionResponse {
    private Double fraudScore;
    private String decision;
    private String reason;
    private String model;
}