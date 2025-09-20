package com.company.app.infrastructure.kafka.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.company.app.infrastructure.kafka.monitoring.KafkaMetricsService;
import com.company.app.modules.taskCore.domain.event.TaskEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Service for publishing task events
 * Handles message publishing with error handling and monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaMetricsService kafkaMetricsService;

    // Topic names
    private static final String TASK_EVENTS_TOPIC = "task-events";
    private static final String TASK_NOTIFICATIONS_TOPIC = "task-notifications";
    private static final String TASK_ANALYTICS_TOPIC = "task-analytics";
    private static final String DEAD_LETTER_QUEUE_TOPIC = "task-events-dlq";

    // ===== TASK EVENTS PUBLISHING =====

    /**
     * Publish task event to the main events topic
     */
    public CompletableFuture<Void> publishTaskEvent(TaskEvent event) {
        return publishTaskEvent(event, TASK_EVENTS_TOPIC);
    }

    /**
     * Publish task event to a specific topic
     */
    public CompletableFuture<Void> publishTaskEvent(TaskEvent event, String topic) {
        log.debug("Publishing task event: {} to topic: {}", event.getEventType(), topic);

        // Set correlation ID if not already set
        if (event.getCorrelationId() == null) {
            event.setCorrelationId(java.util.UUID.randomUUID().toString());
        }

        // Record metrics
        kafkaMetricsService.recordEventPublished(event.getEventType());

        return kafkaTemplate.send(topic, event.getTaskId(), event)
                .thenAccept(result -> {
                    log.info("Successfully published event: {} for task: {} to topic: {}",
                            event.getEventType(), event.getTaskId(), topic);
                    kafkaMetricsService.recordEventPublishedSuccessfully(event.getEventType());
                })
                .exceptionally(throwable -> {
                    log.error("Failed to publish event: {} for task: {} to topic: {}",
                            event.getEventType(), event.getTaskId(), topic, throwable);
                    kafkaMetricsService.recordEventPublishFailure(event.getEventType(), throwable);

                    // Send to dead letter queue
                    sendToDeadLetterQueue(event, throwable);
                    return null;
                });
    }

    // ===== NOTIFICATION EVENTS =====

    /**
     * Publish notification event
     */
    public CompletableFuture<Void> publishNotification(String userId, String message, String type) {
        NotificationEvent notification = NotificationEvent.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Publishing notification for user: {} of type: {}", userId, type);

        return kafkaTemplate.send(TASK_NOTIFICATIONS_TOPIC, userId, notification)
                .thenAccept(result -> {
                    log.info("Successfully published notification for user: {}", userId);
                    kafkaMetricsService.recordEventPublishedSuccessfully("Notification");
                })
                .exceptionally(throwable -> {
                    log.error("Failed to publish notification for user: {}", userId, throwable);
                    kafkaMetricsService.recordEventPublishFailure("Notification", throwable);
                    return null;
                });
    }

    // ===== ANALYTICS EVENTS =====

    /**
     * Publish analytics event
     */
    public CompletableFuture<Void> publishAnalytics(String eventType, String userId, String boardId, Object data) {
        AnalyticsEvent analytics = AnalyticsEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .boardId(boardId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        log.debug("Publishing analytics event: {} for user: {}", eventType, userId);

        return kafkaTemplate.send(TASK_ANALYTICS_TOPIC, userId, analytics)
                .thenAccept(result -> {
                    log.debug("Successfully published analytics event: {} for user: {}", eventType, userId);
                    kafkaMetricsService.recordEventPublishedSuccessfully("Analytics");
                })
                .exceptionally(throwable -> {
                    log.error("Failed to publish analytics event: {} for user: {}", eventType, userId, throwable);
                    kafkaMetricsService.recordEventPublishFailure("Analytics", throwable);
                    return null;
                });
    }

    // ===== DEAD LETTER QUEUE =====

    /**
     * Send failed message to dead letter queue
     */
    private void sendToDeadLetterQueue(TaskEvent event, Throwable throwable) {
        DeadLetterMessage dlqMessage = DeadLetterMessage.builder()
                .originalEvent(event)
                .errorMessage(throwable.getMessage())
                .failedAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        kafkaTemplate.send(DEAD_LETTER_QUEUE_TOPIC, event.getTaskId(), dlqMessage)
                .thenAccept(result -> {
                    log.warn("Message sent to dead letter queue for task: {}", event.getTaskId());
                    kafkaMetricsService.recordMessageSentToDLQ(event.getEventType());
                })
                .exceptionally(dlqThrowable -> {
                    log.error("Failed to send message to dead letter queue for task: {}",
                            event.getTaskId(), dlqThrowable);
                    return null;
                });
    }

    // ===== BATCH OPERATIONS =====

    /**
     * Publish multiple events in batch
     */
    public CompletableFuture<Void> publishBatch(java.util.List<TaskEvent> events) {
        log.info("Publishing batch of {} events", events.size());

        CompletableFuture<Void>[] futures = events.stream()
                .map(this::publishTaskEvent)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
                .thenRun(() -> {
                    log.info("Successfully published batch of {} events", events.size());
                    kafkaMetricsService.recordBatchPublished(events.size());
                })
                .exceptionally(throwable -> {
                    log.error("Failed to publish batch of events", throwable);
                    kafkaMetricsService.recordBatchPublishFailure(events.size(), throwable);
                    return null;
                });
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if Kafka is available
     */
    public boolean isKafkaAvailable() {
        try {
            // Simple health check - try to get metadata
            kafkaTemplate.getProducerFactory().createProducer().partitionsFor("health-check");
            return true;
        } catch (Exception e) {
            log.warn("Kafka health check failed", e);
            return false;
        }
    }

    /**
     * Get Kafka metrics
     */
    public KafkaMetricsService.KafkaMetrics getMetrics() {
        return kafkaMetricsService.getMetrics();
    }

    // ===== INNER CLASSES =====

    /**
     * Notification event for user notifications
     */
    @lombok.Data
    @lombok.Builder
    public static class NotificationEvent {
        private String userId;
        private String message;
        private String type;
        private LocalDateTime timestamp;
        @lombok.Builder.Default
        private String eventId = java.util.UUID.randomUUID().toString();
    }

    /**
     * Analytics event for data analysis
     */
    @lombok.Data
    @lombok.Builder
    public static class AnalyticsEvent {
        private String eventType;
        private String userId;
        private String boardId;
        private Object data;
        private LocalDateTime timestamp;
        @lombok.Builder.Default
        private String eventId = java.util.UUID.randomUUID().toString();
    }

    /**
     * Dead letter queue message for failed events
     */
    @lombok.Data
    @lombok.Builder
    public static class DeadLetterMessage {
        private TaskEvent originalEvent;
        private String errorMessage;
        private LocalDateTime failedAt;
        private int retryCount;
        @lombok.Builder.Default
        private String messageId = java.util.UUID.randomUUID().toString();
    }
}
