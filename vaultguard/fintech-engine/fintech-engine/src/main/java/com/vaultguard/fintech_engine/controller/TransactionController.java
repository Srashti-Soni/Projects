package com.vaultguard.fintech_engine.controller;

import com.vaultguard.fintech_engine.kafka.TransactionProducer;
import com.vaultguard.fintech_engine.model.Transaction;
import com.vaultguard.fintech_engine.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionProducer transactionProducer;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody Transaction transaction) {
        log.info("New transaction request from: {}", transaction.getSenderId());
        Transaction prepared = transactionService.prepareTransaction(transaction);
        transactionProducer.sendTransaction(prepared);
        return ResponseEntity.status(HttpStatus.CREATED).body(prepared);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<Transaction>> getFlaggedTransactions() {
        return ResponseEntity.ok(transactionService.getFlaggedTransactions());
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Transaction>> getBySender(
            @PathVariable String senderId) {
        return ResponseEntity.ok(
                transactionService.getTransactionsBySender(senderId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FinTech Engine is running");
    }
}