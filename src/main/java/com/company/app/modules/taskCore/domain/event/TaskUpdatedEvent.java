package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;
import java.util.Map;

import com.company.app.modules.taskCore.domain.valueobject.Priority;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a task is updated
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskUpdatedEvent extends TaskEvent {

    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime updatedAt;
    private Map<String, Object> changedFields;

    @Builder(builderMethodName = "taskUpdatedEventBuilder")
    public TaskUpdatedEvent(String taskId, String userId, String boardId,
            String title, String description, TaskStatus status,
            Priority priority, String assignedTo, LocalDateTime dueDate,
            Map<String, Object> changedFields) {
        super("TaskUpdated", taskId, userId, boardId);
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
        this.changedFields = changedFields;
    }

    @Override
    public String getEventType() {
        return "TaskUpdated";
    }
}
