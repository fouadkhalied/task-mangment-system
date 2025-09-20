package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a task is assigned to a user
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskAssignedEvent extends TaskEvent {

    private String assignedTo;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private String message;

    @Builder(builderMethodName = "taskAssignedEventBuilder")
    public TaskAssignedEvent(String taskId, String userId, String boardId,
            String assignedTo, String assignedBy, String message) {
        super("TaskAssigned", taskId, userId, boardId);
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.message = message;
        this.assignedAt = LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "TaskAssigned";
    }
}
