package com.company.app.infrastructure.kafka.monitoring;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Metrics Service
 * Collects detailed metrics about Kafka message publishing and consumption
 */
@Service
@Slf4j
public class KafkaMetricsService {

    // Publishing metrics
    private final AtomicLong totalEventsPublished = new AtomicLong(0);
    private final AtomicLong totalEventsPublishedSuccessfully = new AtomicLong(0);
    private final AtomicLong totalEventsPublishFailed = new AtomicLong(0);
    private final AtomicLong totalBatchesPublished = new AtomicLong(0);
    private final AtomicLong totalBatchesPublishFailed = new AtomicLong(0);
    private final AtomicLong totalMessagesSentToDLQ = new AtomicLong(0);

    // Consumption metrics
    private final AtomicLong totalEventsConsumed = new AtomicLong(0);
    private final AtomicLong totalEventsConsumedSuccessfully = new AtomicLong(0);
    private final AtomicLong totalEventsConsumptionFailed = new AtomicLong(0);

    // Event type specific metrics
    private final Map<String, AtomicLong> eventTypePublished = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventTypeConsumed = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventTypePublishFailed = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventTypeConsumptionFailed = new ConcurrentHashMap<>();

    // Timing metrics
    private final Map<String, AtomicLong> eventTypePublishTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventTypeConsumptionTimes = new ConcurrentHashMap<>();

    // Service start time
    private final LocalDateTime startTime = LocalDateTime.now();

    // ===== PUBLISHING METRICS =====

    /**
     * Record that an event was published
     */
    public void recordEventPublished(String eventType) {
        totalEventsPublished.incrementAndGet();
        eventTypePublished.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        log.debug("Event published: {}", eventType);
    }

    /**
     * Record that an event was published successfully
     */
    public void recordEventPublishedSuccessfully(String eventType) {
        totalEventsPublishedSuccessfully.incrementAndGet();
        log.debug("Event published successfully: {}", eventType);
    }

    /**
     * Record that an event publishing failed
     */
    public void recordEventPublishFailure(String eventType, Throwable throwable) {
        totalEventsPublishFailed.incrementAndGet();
        eventTypePublishFailed.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        log.error("Event publish failed: {}", eventType, throwable);
    }

    /**
     * Record that a batch was published
     */
    public void recordBatchPublished(int batchSize) {
        totalBatchesPublished.incrementAndGet();
        log.debug("Batch published with size: {}", batchSize);
    }

    /**
     * Record that a batch publishing failed
     */
    public void recordBatchPublishFailure(int batchSize, Throwable throwable) {
        totalBatchesPublishFailed.incrementAndGet();
        log.error("Batch publish failed with size: {}", batchSize, throwable);
    }

    /**
     * Record that a message was sent to dead letter queue
     */
    public void recordMessageSentToDLQ(String eventType) {
        totalMessagesSentToDLQ.incrementAndGet();
        log.warn("Message sent to DLQ for event type: {}", eventType);
    }

    /**
     * Record publish time for an event type
     */
    public void recordEventPublishTime(String eventType, long publishTimeMs) {
        eventTypePublishTimes.computeIfAbsent(eventType, k -> new AtomicLong(0)).addAndGet(publishTimeMs);
        log.debug("Event publish time recorded: {} - {}ms", eventType, publishTimeMs);
    }

    // ===== CONSUMPTION METRICS =====

    /**
     * Record that an event was consumed
     */
    public void recordEventConsumed(String eventType) {
        totalEventsConsumed.incrementAndGet();
        eventTypeConsumed.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        log.debug("Event consumed: {}", eventType);
    }

    /**
     * Record that an event was consumed successfully
     */
    public void recordEventConsumedSuccessfully(String eventType) {
        totalEventsConsumedSuccessfully.incrementAndGet();
        log.debug("Event consumed successfully: {}", eventType);
    }

    /**
     * Record that an event consumption failed
     */
    public void recordEventConsumptionFailed(String eventType, Throwable throwable) {
        totalEventsConsumptionFailed.incrementAndGet();
        eventTypeConsumptionFailed.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        log.error("Event consumption failed: {}", eventType, throwable);
    }

    /**
     * Record consumption time for an event type
     */
    public void recordEventConsumptionTime(String eventType, long consumptionTimeMs) {
        eventTypeConsumptionTimes.computeIfAbsent(eventType, k -> new AtomicLong(0)).addAndGet(consumptionTimeMs);
        log.debug("Event consumption time recorded: {} - {}ms", eventType, consumptionTimeMs);
    }

    // ===== METRICS RETRIEVAL =====

    /**
     * Get comprehensive Kafka metrics
     */
    public KafkaMetrics getMetrics() {
        long totalPublished = totalEventsPublished.get();
        long totalConsumed = totalEventsConsumed.get();
        long totalSuccessfulPublished = totalEventsPublishedSuccessfully.get();
        long totalSuccessfulConsumed = totalEventsConsumedSuccessfully.get();

        double publishSuccessRate = totalPublished > 0 ? (double) totalSuccessfulPublished / totalPublished : 0.0;
        double consumptionSuccessRate = totalConsumed > 0 ? (double) totalSuccessfulConsumed / totalConsumed : 0.0;

        return KafkaMetrics.builder()
                .totalEventsPublished(totalPublished)
                .totalEventsPublishedSuccessfully(totalSuccessfulPublished)
                .totalEventsPublishFailed(totalEventsPublishFailed.get())
                .totalEventsConsumed(totalConsumed)
                .totalEventsConsumedSuccessfully(totalSuccessfulConsumed)
                .totalEventsConsumptionFailed(totalEventsConsumptionFailed.get())
                .totalBatchesPublished(totalBatchesPublished.get())
                .totalBatchesPublishFailed(totalBatchesPublishFailed.get())
                .totalMessagesSentToDLQ(totalMessagesSentToDLQ.get())
                .publishSuccessRate(publishSuccessRate)
                .consumptionSuccessRate(consumptionSuccessRate)
                .uptimeHours(getUptimeHours())
                .eventTypePublished(Map.copyOf(eventTypePublished))
                .eventTypeConsumed(Map.copyOf(eventTypeConsumed))
                .eventTypePublishFailed(Map.copyOf(eventTypePublishFailed))
                .eventTypeConsumptionFailed(Map.copyOf(eventTypeConsumptionFailed))
                .build();
    }

    /**
     * Get average publish times by event type
     */
    public Map<String, Double> getAveragePublishTimes() {
        Map<String, Double> averages = new ConcurrentHashMap<>();

        eventTypePublishTimes.forEach((eventType, totalTime) -> {
            AtomicLong count = eventTypePublished.get(eventType);
            if (count != null && count.get() > 0) {
                double average = (double) totalTime.get() / count.get();
                averages.put(eventType, average);
            }
        });

        return averages;
    }

    /**
     * Get average consumption times by event type
     */
    public Map<String, Double> getAverageConsumptionTimes() {
        Map<String, Double> averages = new ConcurrentHashMap<>();

        eventTypeConsumptionTimes.forEach((eventType, totalTime) -> {
            AtomicLong count = eventTypeConsumed.get(eventType);
            if (count != null && count.get() > 0) {
                double average = (double) totalTime.get() / count.get();
                averages.put(eventType, average);
            }
        });

        return averages;
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        log.warn("Resetting Kafka metrics");

        totalEventsPublished.set(0);
        totalEventsPublishedSuccessfully.set(0);
        totalEventsPublishFailed.set(0);
        totalEventsConsumed.set(0);
        totalEventsConsumedSuccessfully.set(0);
        totalEventsConsumptionFailed.set(0);
        totalBatchesPublished.set(0);
        totalBatchesPublishFailed.set(0);
        totalMessagesSentToDLQ.set(0);

        eventTypePublished.clear();
        eventTypeConsumed.clear();
        eventTypePublishFailed.clear();
        eventTypeConsumptionFailed.clear();
        eventTypePublishTimes.clear();
        eventTypeConsumptionTimes.clear();
    }

    /**
     * Get service uptime in hours
     */
    private double getUptimeHours() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes() / 60.0;
    }

    // ===== INNER CLASSES =====

    /**
     * Kafka metrics data class
     */
    @lombok.Builder
    @lombok.Data
    public static class KafkaMetrics {
        private long totalEventsPublished;
        private long totalEventsPublishedSuccessfully;
        private long totalEventsPublishFailed;
        private long totalEventsConsumed;
        private long totalEventsConsumedSuccessfully;
        private long totalEventsConsumptionFailed;
        private long totalBatchesPublished;
        private long totalBatchesPublishFailed;
        private long totalMessagesSentToDLQ;
        private double publishSuccessRate;
        private double consumptionSuccessRate;
        private double uptimeHours;
        private Map<String, AtomicLong> eventTypePublished;
        private Map<String, AtomicLong> eventTypeConsumed;
        private Map<String, AtomicLong> eventTypePublishFailed;
        private Map<String, AtomicLong> eventTypeConsumptionFailed;
    }
}
