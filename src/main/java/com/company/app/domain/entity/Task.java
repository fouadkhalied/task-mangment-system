package com.company.app.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.company.app.domain.valueobject.Priority;
import com.company.app.domain.valueobject.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Column(name = "board_id", nullable = false)
    private String boardId;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // PrePersist and PreUpdate callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods expected by service
    public void moveToStatus(TaskStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPriorityWithValidation(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
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
    @Embeddable
    public static class UserInfo {
        @Column(name = "user_id")
        private String userId;

        @Column(name = "user_name", length = 100)
        private String name;

        @Column(name = "user_email", length = 150)
        private String email;
    }
}