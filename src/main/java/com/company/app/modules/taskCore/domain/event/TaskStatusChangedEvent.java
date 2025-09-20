package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;

import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a task status is changed
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskStatusChangedEvent extends TaskEvent {

    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private LocalDateTime changedAt;
    private String reason;

    @Builder(builderMethodName = "taskStatusChangedEventBuilder")
    public TaskStatusChangedEvent(String taskId, String userId, String boardId,
            TaskStatus oldStatus, TaskStatus newStatus, String reason) {
        super("TaskStatusChanged", taskId, userId, boardId);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedAt = LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "TaskStatusChanged";
    }
}
