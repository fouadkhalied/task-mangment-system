package com.company.app.application.dto;

import com.company.app.domain.valueobject.Priority;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {
    
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Board ID is required")
    private String boardId;
    
    private Priority priority = Priority.MEDIUM;
    
    private LocalDateTime dueDate;
    
    private String assignedTo;
}

