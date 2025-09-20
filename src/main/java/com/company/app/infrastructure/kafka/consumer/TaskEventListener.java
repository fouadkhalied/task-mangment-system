package com.company.app.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.company.app.infrastructure.kafka.monitoring.KafkaMetricsService;
import com.company.app.modules.taskCore.domain.event.TaskAssignedEvent;
import com.company.app.modules.taskCore.domain.event.TaskCreatedEvent;
import com.company.app.modules.taskCore.domain.event.TaskDeletedEvent;
import com.company.app.modules.taskCore.domain.event.TaskEvent;
import com.company.app.modules.taskCore.domain.event.TaskOverdueEvent;
import com.company.app.modules.taskCore.domain.event.TaskStatusChangedEvent;
import com.company.app.modules.taskCore.domain.event.TaskUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Event Listener
 * Handles incoming task events from Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final KafkaMetricsService kafkaMetricsService;
    private final TaskEventProcessor taskEventProcessor;

    /**
     * Listen to task events topic
     */
    @KafkaListener(topics = "task-events", groupId = "task-management-group")
    public void handleTaskEvent(@Payload TaskEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("Received task event: {} for task: {} from topic: {}, partition: {}, offset: {}",
                    event.getEventType(), event.getTaskId(), topic, partition, offset);

            // Record consumption metrics
            kafkaMetricsService.recordEventConsumed(event.getEventType());

            // Process the event based on its type
            processTaskEvent(event);

            // Record successful consumption
            kafkaMetricsService.recordEventConsumedSuccessfully(event.getEventType());

            // Manually acknowledge the message
            acknowledgment.acknowledge();

            // Record processing time
            long processingTime = System.currentTimeMillis() - startTime;
            kafkaMetricsService.recordEventConsumptionTime(event.getEventType(), processingTime);

            log.info("Successfully processed task event: {} for task: {} in {}ms",
                    event.getEventType(), event.getTaskId(), processingTime);

        } catch (Exception e) {
            log.error("Failed to process task event: {} for task: {}",
                    event.getEventType(), event.getTaskId(), e);

            // Record consumption failure
            kafkaMetricsService.recordEventConsumptionFailed(event.getEventType(), e);

            // Don't acknowledge - let it retry or go to DLQ
            // In production, you might want to implement retry logic here
        }
    }

    /**
     * Listen to task notifications topic
     */
    @KafkaListener(topics = "task-notifications", groupId = "notification-group")
    public void handleNotification(@Payload Object notification,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received notification from topic: {}", topic);
            log.debug("Notification content: {}", notification);

            // Process notification (e.g., send email, push notification, etc.)
            taskEventProcessor.processNotification(notification);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process notification from topic: {}", topic, e);
            // Don't acknowledge - let it retry
        }
    }

    /**
     * Listen to task analytics topic
     */
    @KafkaListener(topics = "task-analytics", groupId = "analytics-group")
    public void handleAnalytics(@Payload Object analytics,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.debug("Received analytics event from topic: {}", topic);
            log.debug("Analytics content: {}", analytics);

            // Process analytics (e.g., store in analytics database, send to BI tools)
            taskEventProcessor.processAnalytics(analytics);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process analytics from topic: {}", topic, e);
            // Don't acknowledge - let it retry
        }
    }

    /**
     * Listen to dead letter queue
     */
    @KafkaListener(topics = "task-events-dlq", groupId = "dlq-group")
    public void handleDeadLetterMessage(@Payload Object dlqMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.warn("Received dead letter message from topic: {}", topic);
            log.warn("DLQ message content: {}", dlqMessage);

            // Process DLQ message (e.g., alert administrators, store for manual review)
            taskEventProcessor.processDeadLetterMessage(dlqMessage);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process dead letter message from topic: {}", topic, e);
            // Don't acknowledge - this should be investigated manually
        }
    }

    /**
     * Process task event based on its type
     */
    private void processTaskEvent(TaskEvent event) {
        switch (event.getEventType()) {
            case "TaskCreated" -> taskEventProcessor.processTaskCreated((TaskCreatedEvent) event);
            case "TaskUpdated" -> taskEventProcessor.processTaskUpdated((TaskUpdatedEvent) event);
            case "TaskStatusChanged" -> taskEventProcessor.processTaskStatusChanged((TaskStatusChangedEvent) event);
            case "TaskDeleted" -> taskEventProcessor.processTaskDeleted((TaskDeletedEvent) event);
            case "TaskAssigned" -> taskEventProcessor.processTaskAssigned((TaskAssignedEvent) event);
            case "TaskOverdue" -> taskEventProcessor.processTaskOverdue((TaskOverdueEvent) event);
            default -> {
                log.warn("Unknown event type: {} for task: {}", event.getEventType(), event.getTaskId());
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
            }
        }
    }
}
