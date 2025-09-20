package com.company.app.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.company.app.infrastructure.kafka.monitoring.KafkaMetricsService;
import com.company.app.modules.taskCore.domain.event.TaskCreatedEvent;
import com.company.app.modules.taskCore.domain.valueobject.Priority;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

/**
 * Unit tests for Kafka service (without Kafka dependency)
 * Tests event creation and basic functionality
 */
class KafkaServiceUnitTest {

    @Test
    void testTaskCreatedEventCreation() {
        // Test task created event creation
        TaskCreatedEvent event = TaskCreatedEvent.taskCreatedEventBuilder()
                .taskId("test-task-1")
                .userId("user-1")
                .boardId("board-1")
                .title("Test Task")
                .description("Test task description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .assignedTo("user-1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        assertNotNull(event);
        assertEquals("test-task-1", event.getTaskId());
        assertEquals("user-1", event.getUserId());
        assertEquals("board-1", event.getBoardId());
        assertEquals("Test Task", event.getTitle());
        assertEquals("Test task description", event.getDescription());
        assertEquals(TaskStatus.TODO, event.getStatus());
        assertEquals(Priority.MEDIUM, event.getPriority());
        assertEquals("user-1", event.getAssignedTo());
        assertEquals("TaskCreated", event.getEventType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertNotNull(event.getCreatedAt());
    }

    @Test
    void testKafkaMetricsStructure() {
        // Test that Kafka metrics can be created
        KafkaMetricsService.KafkaMetrics metrics = KafkaMetricsService.KafkaMetrics.builder()
                .totalEventsPublished(100L)
                .totalEventsPublishedSuccessfully(95L)
                .totalEventsPublishFailed(5L)
                .totalEventsConsumed(80L)
                .totalEventsConsumedSuccessfully(75L)
                .totalEventsConsumptionFailed(5L)
                .totalBatchesPublished(10L)
                .totalBatchesPublishFailed(1L)
                .totalMessagesSentToDLQ(2L)
                .publishSuccessRate(0.95)
                .consumptionSuccessRate(0.9375)
                .uptimeHours(2.5)
                .build();

        assertNotNull(metrics);
        assertEquals(100L, metrics.getTotalEventsPublished());
        assertEquals(95L, metrics.getTotalEventsPublishedSuccessfully());
        assertEquals(5L, metrics.getTotalEventsPublishFailed());
        assertEquals(80L, metrics.getTotalEventsConsumed());
        assertEquals(75L, metrics.getTotalEventsConsumedSuccessfully());
        assertEquals(5L, metrics.getTotalEventsConsumptionFailed());
        assertEquals(10L, metrics.getTotalBatchesPublished());
        assertEquals(1L, metrics.getTotalBatchesPublishFailed());
        assertEquals(2L, metrics.getTotalMessagesSentToDLQ());
        assertEquals(0.95, metrics.getPublishSuccessRate());
        assertEquals(0.9375, metrics.getConsumptionSuccessRate());
        assertEquals(2.5, metrics.getUptimeHours());
    }

    @Test
    void testEventInheritance() {
        // Test that TaskCreatedEvent properly extends TaskEvent
        TaskCreatedEvent event = TaskCreatedEvent.taskCreatedEventBuilder()
                .taskId("test-task-1")
                .userId("user-1")
                .boardId("board-1")
                .title("Test Task")
                .build();

        // Verify inheritance
        assertTrue(event instanceof TaskCreatedEvent);
        assertTrue(event instanceof com.company.app.modules.taskCore.domain.event.TaskEvent);

        // Test inherited properties
        assertEquals("test-task-1", event.getTaskId());
        assertEquals("user-1", event.getUserId());
        assertEquals("board-1", event.getBoardId());
        assertEquals("TaskCreated", event.getEventType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }
}
