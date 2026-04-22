package com.vaultguard.fintech_engine.kafka;

import com.vaultguard.fintech_engine.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public void sendTransaction(Transaction transaction) {
        log.info("Sending transaction to Kafka: {}",
                transaction.getTransactionId());
        kafkaTemplate.send("transaction-events",
                transaction.getTransactionId(),
                transaction);
        log.info("Transaction sent to topic: transaction-events");
    }

    public void sendFraudAlert(Transaction transaction) {
        log.info("Sending fraud alert to Kafka: {}",
                transaction.getTransactionId());
        kafkaTemplate.send("fraud-alert-events",
                transaction.getTransactionId(),
                transaction);
        log.info("Fraud alert sent to topic: fraud-alert-events");
    }
}