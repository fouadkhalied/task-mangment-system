package com.company.app.application.service;

import com.company.app.domain.entity.Task;
import com.company.app.domain.repository.TaskRepository;
import com.company.app.domain.valueobject.TaskStatus;
import com.company.app.application.dto.TaskCreateRequest;
import com.company.app.application.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskApplicationService {
    
    private final TaskRepository taskRepository;
    
    public TaskResponse createTask(TaskCreateRequest request) {
        log.info("Creating task: {}", request.getTitle());
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setBoardId(request.getBoardId());
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.TODO); // Set default status
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // Apply business rules
        if (request.getPriority() != null) {
            task.setPriorityWithValidation(request.getPriority());
        }
        
        // Set assignee if provided
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
        
        return mapToResponse(savedTask);
    }
    
    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
            
        task.moveToStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now()); // Track update time
        
        Task updatedTask = taskRepository.save(task);
        
        log.info("Task {} status updated to {}", taskId, newStatus);
        return mapToResponse(updatedTask);
    }
    
    public TaskResponse updateTask(String taskId, TaskCreateRequest request) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
            
        // Update fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
        
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
    
    public void deleteTask(String taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException("Task not found: " + taskId);
        }
        
        taskRepository.deleteById(taskId);
        log.info("Task {} deleted", taskId);
    }
    
    public TaskResponse getTask(String taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
            
        return mapToResponse(task);
    }
    
    public List<TaskResponse> getTasksByBoard(String boardId) {
        return taskRepository.findByBoardId(boardId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksByAssignee(String userId) {
        return taskRepository.findByAssignedTo(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public long getTaskCount(String boardId, TaskStatus status) {
        return taskRepository.countByBoardIdAndStatus(boardId, status);
    }
    
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
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