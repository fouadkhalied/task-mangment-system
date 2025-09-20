package com.company.app.modules.taskCore.domain.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event published when a task becomes overdue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskOverdueEvent extends TaskEvent {

    private String title;
    private LocalDateTime dueDate;
    private LocalDateTime overdueSince;
    private int daysOverdue;

    @Builder(builderMethodName = "taskOverdueEventBuilder")
    public TaskOverdueEvent(String taskId, String userId, String boardId,
            String title, LocalDateTime dueDate, int daysOverdue) {
        super("TaskOverdue", taskId, userId, boardId);
        this.title = title;
        this.dueDate = dueDate;
        this.overdueSince = LocalDateTime.now();
        this.daysOverdue = daysOverdue;
    }

    @Override
    public String getEventType() {
        return "TaskOverdue";
    }
}
