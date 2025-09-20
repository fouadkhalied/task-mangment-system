package com.company.app.modules.taskCore.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.app.infrastructure.kafka.monitoring.KafkaMetricsService;
import com.company.app.infrastructure.kafka.service.KafkaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Management Controller
 * Provides REST APIs for monitoring and managing Kafka operations
 */
@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KafkaManagementController {

    private final KafkaService kafkaService;
    private final KafkaMetricsService kafkaMetricsService;

    /**
     * Get Kafka health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> getKafkaHealth() {
        boolean isAvailable = kafkaService.isKafkaAvailable();
        if (isAvailable) {
            return ResponseEntity.ok("Kafka is running normally");
        } else {
            return ResponseEntity.status(503).body("Kafka is not available");
        }
    }

    /**
     * Get Kafka metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<KafkaMetricsService.KafkaMetrics> getKafkaMetrics() {
        log.info("Getting Kafka performance metrics");
        KafkaMetricsService.KafkaMetrics metrics = kafkaMetricsService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get average publish times by event type
     */
    @GetMapping("/metrics/publish-times")
    public ResponseEntity<java.util.Map<String, Double>> getAveragePublishTimes() {
        log.info("Getting average Kafka publish times");
        java.util.Map<String, Double> averages = kafkaMetricsService.getAveragePublishTimes();
        return ResponseEntity.ok(averages);
    }

    /**
     * Get average consumption times by event type
     */
    @GetMapping("/metrics/consumption-times")
    public ResponseEntity<java.util.Map<String, Double>> getAverageConsumptionTimes() {
        log.info("Getting average Kafka consumption times");
        java.util.Map<String, Double> averages = kafkaMetricsService.getAverageConsumptionTimes();
        return ResponseEntity.ok(averages);
    }

    /**
     * Reset Kafka metrics
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<String> resetKafkaMetrics() {
        log.warn("Resetting Kafka metrics by admin request");
        kafkaMetricsService.resetMetrics();
        return ResponseEntity.ok("Kafka metrics reset successfully");
    }

    /**
     * Get Kafka service information
     */
    @GetMapping("/info")
    public ResponseEntity<java.util.Map<String, Object>> getKafkaInfo() {
        log.info("Getting Kafka service information");

        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("service", "Kafka Messaging Service");
        info.put("version", "1.0.0");
        info.put("topics", java.util.List.of(
                "task-events",
                "task-notifications",
                "task-analytics",
                "task-events-dlq"));
        info.put("available", kafkaService.isKafkaAvailable());
        info.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(info);
    }
}
