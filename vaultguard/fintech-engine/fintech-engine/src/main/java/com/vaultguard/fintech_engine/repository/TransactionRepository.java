package com.vaultguard.fintech_engine.repository;

import com.vaultguard.fintech_engine.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions by sender
    List<Transaction> findBySenderId(String senderId);

    // Find all transactions by status
    List<Transaction> findByStatus(String status);

    // Find all flagged transactions
    List<Transaction> findByFraudScoreGreaterThan(Double threshold);

    // Find by payment type
    List<Transaction> findByPaymentType(String paymentType);
}