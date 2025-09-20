package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for all task-related events
 * Provides common fields and structure for domain events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TaskEvent {

    private String eventId;
    private String eventType;
    private String taskId;
    private String userId;
    private String boardId;
    private LocalDateTime timestamp;
    private String correlationId;
    private String causationId;
    private int version;

    /**
     * Constructor for creating events with basic information
     */
    protected TaskEvent(String eventType, String taskId, String userId, String boardId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.taskId = taskId;
        this.userId = userId;
        this.boardId = boardId;
        this.timestamp = LocalDateTime.now();
        this.version = 1;
    }

    /**
     * Get the event type for routing and handling
     */
    public abstract String getEventType();
}
