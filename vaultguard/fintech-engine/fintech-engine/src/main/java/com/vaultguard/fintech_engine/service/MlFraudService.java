package com.vaultguard.fintech_engine.service;

import com.vaultguard.fintech_engine.dto.MlPredictionResponse;
import com.vaultguard.fintech_engine.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlFraudService {

    private final WebClient webClient;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public MlPredictionResponse predict(Transaction transaction) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", transaction.getAmount());
            requestBody.put("location", transaction.getLocation());
            requestBody.put("paymentType", transaction.getPaymentType());

            MlPredictionResponse response = webClient
                    .post()
                    .uri(mlServiceUrl + "/predict")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(MlPredictionResponse.class)
                    .block();

            log.info("ML Prediction → Score: {} | Decision: {}",
                    response.getFraudScore(),
                    response.getDecision());

            return response;

        } catch (Exception e) {
            log.error("ML Service unavailable — falling back to rules: {}",
                    e.getMessage());
            return null;
        }
    }
}