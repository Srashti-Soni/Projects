package com.vaultguard.fintech_engine.kafka;

import com.vaultguard.fintech_engine.model.Transaction;
import com.vaultguard.fintech_engine.service.AlertService;
import com.vaultguard.fintech_engine.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final TransactionProducer transactionProducer;
    private final AlertService alertService;

    @KafkaListener(topics = "transaction-events",
            groupId = "fintech-group")
    public void consumeTransaction(Transaction transaction) {
        log.info("Transaction received from Kafka: {}",
                transaction.getTransactionId());

        // Fraud score lagao aur save karo
        Transaction processed =
                transactionService.processTransaction(transaction);

        // Har transaction ka update React ko bhejo
        alertService.sendTransactionUpdate(processed);

        // Agar suspicious hai toh fraud alert bhi bhejo
        if (processed.getStatus().equals("FLAGGED") ||
                processed.getStatus().equals("HELD")) {
            transactionProducer.sendFraudAlert(processed);
            alertService.sendFraudAlert(processed);
            log.warn("Fraud alert sent for: {}",
                    processed.getTransactionId());
        }
    }
}