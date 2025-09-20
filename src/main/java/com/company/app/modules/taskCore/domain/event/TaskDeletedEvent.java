package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a task is deleted
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDeletedEvent extends TaskEvent {

    private String title;
    private String reason;
    private LocalDateTime deletedAt;

    @Builder(builderMethodName = "taskDeletedEventBuilder")
    public TaskDeletedEvent(String taskId, String userId, String boardId,
            String title, String reason) {
        super("TaskDeleted", taskId, userId, boardId);
        this.title = title;
        this.reason = reason;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "TaskDeleted";
    }
}
