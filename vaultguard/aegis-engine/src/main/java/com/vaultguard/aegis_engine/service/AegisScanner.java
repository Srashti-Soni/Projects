package com.vaultguard.aegis_engine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AegisScanner {

    // ── PATTERNS ──
    // Yeh patterns detect karte hain sensitive data

    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "(api[_-]?key|apikey|api[_-]?secret)\\s*[=:]\\s*['\"]?([a-zA-Z0-9_\\-]{16,})['\"]?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(password|passwd|pwd|secret|token)\\s*[=:]\\s*['\"]?([^\\s'\"]{6,})['\"]?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DB_CONNECTION_PATTERN = Pattern.compile(
            "(jdbc:|mongodb://|mysql://|postgresql://|redis://)(\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile(
            "(AKIA|AGPA|AROA|ASCA|ASIA)[A-Z0-9]{16}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+91|91)?[6-9][0-9]{9}"
    );

    private static final Pattern AADHAR_PATTERN = Pattern.compile(
            "[2-9][0-9]{3}\\s?[0-9]{4}\\s?[0-9]{4}"
    );

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "[0-9]{4}[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{4}"
    );

    // ── SCAN METHOD ──
    public ScanResult scan(String code) {
        List<String> detectedPatterns = new ArrayList<>();
        String sensitivityType = "CLEAN";
        double riskScore = 0.0;
        String redactedCode = code;

        // API Key check
        if (API_KEY_PATTERN.matcher(code).find()) {
            detectedPatterns.add("API Key detected");
            sensitivityType = "API_KEY";
            riskScore = Math.max(riskScore, 0.85);
            redactedCode = API_KEY_PATTERN.matcher(redactedCode)
                    .replaceAll("$1=[REDACTED_API_KEY]");
        }

        // Password check
        if (PASSWORD_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Password/Secret detected");
            sensitivityType = "PASSWORD";
            riskScore = Math.max(riskScore, 0.90);
            redactedCode = PASSWORD_PATTERN.matcher(redactedCode)
                    .replaceAll("$1=[REDACTED_SECRET]");
        }

        // DB connection check
        if (DB_CONNECTION_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Database connection string detected");
            sensitivityType = "DB_SCHEMA";
            riskScore = Math.max(riskScore, 0.80);
            redactedCode = DB_CONNECTION_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_DB_URL]");
        }

        // Private key check
        if (PRIVATE_KEY_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Private key detected");
            sensitivityType = "PRIVATE_KEY";
            riskScore = Math.max(riskScore, 0.99);
            redactedCode = redactedCode.replaceAll(
                    "-----BEGIN.*?PRIVATE KEY-----[\\s\\S]*?-----END.*?PRIVATE KEY-----",
                    "[REDACTED_PRIVATE_KEY]"
            );
        }

        // AWS key check
        if (AWS_KEY_PATTERN.matcher(code).find()) {
            detectedPatterns.add("AWS Access Key detected");
            sensitivityType = "AWS_KEY";
            riskScore = Math.max(riskScore, 0.95);
            redactedCode = AWS_KEY_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_AWS_KEY]");
        }

        // Email PII check
        Matcher emailMatcher = EMAIL_PATTERN.matcher(code);
        int emailCount = 0;
        while (emailMatcher.find()) emailCount++;
        if (emailCount > 2) {
            detectedPatterns.add("Multiple emails detected — possible PII leak");
            sensitivityType = "PII";
            riskScore = Math.max(riskScore, 0.70);
            redactedCode = EMAIL_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_EMAIL]");
        }

        // Phone number check
        if (PHONE_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Phone number detected");
            if (sensitivityType.equals("CLEAN")) sensitivityType = "PII";
            riskScore = Math.max(riskScore, 0.65);
            redactedCode = PHONE_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_PHONE]");
        }

        // Aadhar check
        if (AADHAR_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Aadhar number detected");
            sensitivityType = "PII";
            riskScore = Math.max(riskScore, 0.95);
            redactedCode = AADHAR_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_AADHAR]");
        }

        // Credit card check
        if (CREDIT_CARD_PATTERN.matcher(code).find()) {
            detectedPatterns.add("Credit card number detected");
            sensitivityType = "PII";
            riskScore = Math.max(riskScore, 0.99);
            redactedCode = CREDIT_CARD_PATTERN.matcher(redactedCode)
                    .replaceAll("[REDACTED_CARD_NUMBER]");
        }

        // Final result
        String result;
        String action;

        if (riskScore >= 0.85) {
            result = "BLOCKED";
            action = "Code blocked — sensitive data cannot be sent to AI";
        } else if (riskScore >= 0.5) {
            result = "SENSITIVE";
            action = "Code redacted — sensitive parts removed before sending";
        } else {
            result = "SAFE";
            action = "Code is clean — safe to use with AI tools";
            redactedCode = code;
        }

        log.info("Scan complete → Result: {} | Risk: {} | Patterns: {}",
                result, riskScore, detectedPatterns);

        return new ScanResult(
                result, sensitivityType, riskScore,
                redactedCode, detectedPatterns, action
        );
    }

    // ── INNER CLASS ──
    public static class ScanResult {
        public String result;
        public String sensitivityType;
        public double riskScore;
        public String redactedCode;
        public List<String> detectedPatterns;
        public String action;

        public ScanResult(String result, String sensitivityType,
                          double riskScore, String redactedCode,
                          List<String> detectedPatterns, String action) {
            this.result = result;
            this.sensitivityType = sensitivityType;
            this.riskScore = riskScore;
            this.redactedCode = redactedCode;
            this.detectedPatterns = detectedPatterns;
            this.action = action;
        }
    }
}