package com.vaultguard.fintech_engine.service;

import com.vaultguard.fintech_engine.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendFraudAlert(Transaction transaction) {
        log.info("Sending WebSocket alert for: {}",
                transaction.getTransactionId());

        // React dashboard ke /topic/fraud-alerts pe bhejo
        messagingTemplate.convertAndSend(
                "/topic/fraud-alerts", transaction);

        log.info("WebSocket alert sent successfully");
    }

    public void sendTransactionUpdate(Transaction transaction) {
        // React dashboard ke /topic/transactions pe bhejo
        messagingTemplate.convertAndSend(
                "/topic/transactions", transaction);
    }
}