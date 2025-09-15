package com.company.app.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.app.application.dto.TaskCreateRequest;
import com.company.app.application.dto.TaskResponse;
import com.company.app.domain.entity.Task;
import com.company.app.domain.repository.TaskRepository;
import com.company.app.domain.valueobject.Priority;
import com.company.app.domain.valueobject.TaskStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskApplicationService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        log.info("Creating task: {}", request.getTitle());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setBoardId(request.getBoardId());
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.TODO); // Set default status
        // Note: createdAt and updatedAt are automatically set by @PrePersist

        // Apply business rules
        if (request.getPriority() != null) {
            task.setPriorityWithValidation(request.getPriority());
        } else {
            task.setPriority(Priority.MEDIUM); // Set default priority if not provided
        }

        // Set assignee if provided
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());

        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        task.moveToStatus(newStatus);
        // updatedAt is automatically set by @PreUpdate

        Task updatedTask = taskRepository.save(task);

        log.info("Task {} status updated to {}", taskId, newStatus);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTask(String taskId, TaskCreateRequest request) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        // Update fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        // updatedAt is automatically set by @PreUpdate

        if (request.getPriority() != null) {
            task.setPriorityWithValidation(request.getPriority());
        }

        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} updated", taskId);

        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        if (!taskRepository.existsById(UUID.fromString(taskId))) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }

        taskRepository.deleteById(UUID.fromString(taskId));
        log.info("Task {} deleted", taskId);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByBoard(String boardId) {
        return taskRepository.findByBoardId(boardId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignee(String userId) {
        return taskRepository.findByAssignedTo(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTaskCount(String boardId, TaskStatus status) {
        return taskRepository.countByBoardIdAndStatus(boardId, status);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByBoardAndStatus(String boardId, TaskStatus status) {
        return taskRepository.findByBoardIdAndStatus(boardId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByBoardOrderByCreatedDate(String boardId) {
        return taskRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssigneeAndStatus(String userId, TaskStatus status) {
        return taskRepository.findByAssignedToAndStatus(userId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId() != null ? task.getId().toString() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignedTo(task.getAssignedTo())
                .dueDate(task.getDueDate())
                .boardId(task.getBoardId())
                .overdue(task.isOverdue())
                .build();
    }
}

// Custom exception for better error handling
class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}