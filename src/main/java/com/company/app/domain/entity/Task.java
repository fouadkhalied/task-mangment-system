package com.company.app.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.company.app.domain.valueobject.TaskStatus;
import com.company.app.domain.valueobject.Priority;

import java.time.LocalDateTime;

@Document(collection = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    private String id;
    
    @Field("title")
    private String title;
    
    @Field("description")
    private String description;
    
    @Field("status")
    private TaskStatus status;
    
    @Field("priority")
    private Priority priority;
    
    @Field("board_id")
    private String boardId;  // Changed from projectId to match service
    
    @Field("assigned_to")
    private String assignedTo;  // Simple string reference to user ID
    
    @Field("due_date")
    private LocalDateTime dueDate;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // Business logic methods expected by service
    public void moveToStatus(TaskStatus newStatus) {
        // Add validation logic if needed
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPriorityWithValidation(Priority priority) {
        // Add business validation if needed
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.priority = priority;
    }
    
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate) && 
               status != TaskStatus.DONE;
    }
    
    // Embedded document for assignee details (optional - can be used later)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String userId;
        private String name;
        private String email;
    }
}