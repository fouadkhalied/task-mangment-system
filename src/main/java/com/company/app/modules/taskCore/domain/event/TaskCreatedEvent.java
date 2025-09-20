package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;

import com.company.app.modules.taskCore.domain.valueobject.Priority;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a new task is created
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskCreatedEvent extends TaskEvent {

    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;

    @Builder(builderMethodName = "taskCreatedEventBuilder")
    public TaskCreatedEvent(String taskId, String userId, String boardId,
            String title, String description, TaskStatus status,
            Priority priority, String assignedTo, LocalDateTime dueDate) {
        super("TaskCreated", taskId, userId, boardId);
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "TaskCreated";
    }
}
