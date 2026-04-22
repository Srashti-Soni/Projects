package com.vaultguard.fintech_engine.service;

import com.vaultguard.fintech_engine.dto.MlPredictionResponse;
import com.vaultguard.fintech_engine.model.Transaction;
import com.vaultguard.fintech_engine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // Sirf ID aur timestamp set karta hai
    // Kafka pe bhejne ka kaam Controller karega
    public Transaction prepareTransaction(Transaction transaction) {
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("PENDING");
        return transaction;
    }

    private final MlFraudService mlFraudService;

    public Transaction processTransaction(Transaction transaction) {
        double finalScore;
        String finalReason;
        String finalStatus;

        // ML model se score lo
        MlPredictionResponse mlResponse = mlFraudService.predict(transaction);

        if (mlResponse != null) {
            // ML score available hai
            double mlScore = mlResponse.getFraudScore();

            // Rule based score bhi calculate karo
            double ruleScore = calculateFraudScore(transaction);

            // Dono ka average lo — better accuracy
            finalScore = (mlScore * 0.7) + (ruleScore * 0.3);

            log.info("ML Score: {} | Rule Score: {} | Final: {}",
                    mlScore, ruleScore, finalScore);
        } else {
            // ML service down hai — sirf rules use karo
            finalScore = calculateFraudScore(transaction);
            log.warn("ML service unavailable — using rules only");
        }

        // Final decision
        if (finalScore >= 0.75) {
            finalStatus = "HELD";
            finalReason = "High fraud risk detected by AI";
        } else if (finalScore >= 0.5) {
            finalStatus = "FLAGGED";
            finalReason = "Suspicious pattern detected by AI";
        } else {
            finalStatus = "APPROVED";
            finalReason = null;
        }

        transaction.setFraudScore(finalScore);
        transaction.setStatus(finalStatus);
        transaction.setFraudReason(finalReason);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getFlaggedTransactions() {
        return transactionRepository.findByFraudScoreGreaterThan(0.5);
    }

    public List<Transaction> getTransactionsBySender(String senderId) {
        return transactionRepository.findBySenderId(senderId);
    }

    private double calculateFraudScore(Transaction transaction) {
        double score = 0.0;

        if (transaction.getAmount() > 100000) {
            score += 0.4;
        } else if (transaction.getAmount() > 50000) {
            score += 0.2;
        }

        if (transaction.getLocation() != null &&
                transaction.getLocation().equalsIgnoreCase("INTERNATIONAL")) {
            score += 0.3;
        }

        if (transaction.getPaymentType().equalsIgnoreCase("WALLET")
                && transaction.getAmount() > 10000) {
            score += 0.2;
        }

        int hour = LocalDateTime.now().getHour();
        if (hour >= 23 || hour <= 4) {
            score += 0.1;
        }

        return Math.min(score, 1.0);
    }
}