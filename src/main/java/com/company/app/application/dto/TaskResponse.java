package com.company.app.application.dto;

import com.company.app.domain.valueobject.TaskStatus;
import com.company.app.domain.valueobject.Priority;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private String boardId;
    private boolean overdue;
}

