package com.vaultguard.fintech_engine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {

    @Bean
    public NewTopic transactionTopic() {
        return TopicBuilder.name("transaction-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fraudAlertTopic() {
        return TopicBuilder.name("fraud-alert-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic codeAuditTopic() {
        return TopicBuilder.name("code-audit-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}