package com.vaultguard.fintech_engine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String receiverId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    // UPI, CARD, WALLET
    @Column(nullable = false)
    private String paymentType;

    // PENDING, APPROVED, FLAGGED, BLOCKED
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Fraud score between 0.0 to 1.0
    // Above 0.7 means suspicious
    private Double fraudScore;

    private String fraudReason;
}