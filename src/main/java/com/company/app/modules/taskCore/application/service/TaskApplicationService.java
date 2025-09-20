package com.company.app.modules.taskCore.application.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.app.infrastructure.kafka.service.KafkaService;
import com.company.app.modules.taskCore.application.dto.TaskCreateRequest;
import com.company.app.modules.taskCore.application.dto.TaskResponse;
import com.company.app.modules.taskCore.domain.entity.Task;
import com.company.app.modules.taskCore.domain.event.TaskCreatedEvent;
import com.company.app.modules.taskCore.domain.event.TaskDeletedEvent;
import com.company.app.modules.taskCore.domain.event.TaskOverdueEvent;
import com.company.app.modules.taskCore.domain.event.TaskStatusChangedEvent;
import com.company.app.modules.taskCore.domain.event.TaskUpdatedEvent;
import com.company.app.modules.taskCore.domain.repository.TaskRepository;
import com.company.app.modules.taskCore.domain.valueobject.Priority;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;
import com.company.app.modules.taskCore.infrastructure.cache.TaskCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskApplicationService {

    private final TaskRepository taskRepository;
    private final TaskCacheService taskCacheService;
    private final KafkaService kafkaService;

    @Transactional
    @CacheEvict(value = { "taskLists", "taskCounts" }, allEntries = true)
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

        TaskResponse response = mapToResponse(savedTask);

        // Cache the new task
        taskCacheService.cacheTask(savedTask.getId().toString(), response);

        // Evict related caches
        if (request.getAssignedTo() != null) {
            taskCacheService.evictUserTasks(request.getAssignedTo());
        }
        taskCacheService.evictBoardCache(request.getBoardId());
        taskCacheService.evictOverdueTasks();

        // Publish task created event
        publishTaskCreatedEvent(savedTask, request);

        return response;
    }

    @Transactional
    @CacheEvict(value = { "taskLists", "taskCounts" }, allEntries = true)
    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        TaskStatus oldStatus = task.getStatus();
        task.moveToStatus(newStatus);
        // updatedAt is automatically set by @PreUpdate

        Task updatedTask = taskRepository.save(task);
        TaskResponse response = mapToResponse(updatedTask);

        log.info("Task {} status updated from {} to {}", taskId, oldStatus, newStatus);

        // Update cache with new task data
        taskCacheService.cacheTask(taskId, response);

        // Evict related caches
        taskCacheService.evictBoardCache(task.getBoardId());
        if (task.getAssignedTo() != null) {
            taskCacheService.evictUserTasks(task.getAssignedTo());
        }
        taskCacheService.evictOverdueTasks();

        // Publish task status changed event
        publishTaskStatusChangedEvent(task, oldStatus, newStatus);

        return response;
    }

    @Transactional
    @CacheEvict(value = { "taskLists", "taskCounts" }, allEntries = true)
    public TaskResponse updateTask(String taskId, TaskCreateRequest request) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        String oldAssignee = task.getAssignedTo();
        String oldBoardId = task.getBoardId();

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
        TaskResponse response = mapToResponse(updatedTask);

        log.info("Task {} updated", taskId);

        // Update cache with new task data
        taskCacheService.cacheTask(taskId, response);

        // Evict related caches
        taskCacheService.evictBoardCache(oldBoardId);
        if (request.getBoardId() != null && !request.getBoardId().equals(oldBoardId)) {
            taskCacheService.evictBoardCache(request.getBoardId());
        }

        if (oldAssignee != null) {
            taskCacheService.evictUserTasks(oldAssignee);
        }
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(oldAssignee)) {
            taskCacheService.evictUserTasks(request.getAssignedTo());
        }
        taskCacheService.evictOverdueTasks();

        // Publish task updated event
        publishTaskUpdatedEvent(updatedTask, request, oldAssignee, oldBoardId);

        return response;
    }

    @Transactional
    @CacheEvict(value = { "tasks", "taskLists", "taskCounts" }, allEntries = true)
    public void deleteTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }

        // Get task info before deletion for cache eviction
        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        String boardId = task.getBoardId();
        String assignee = task.getAssignedTo();

        taskRepository.deleteById(UUID.fromString(taskId));
        log.info("Task {} deleted", taskId);

        // Evict task from cache
        taskCacheService.evictTask(taskId);

        // Evict related caches
        taskCacheService.evictBoardCache(boardId);
        if (assignee != null) {
            taskCacheService.evictUserTasks(assignee);
        }
        taskCacheService.evictOverdueTasks();

        // Publish task deleted event
        publishTaskDeletedEvent(task);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId", unless = "#result == null")
    public TaskResponse getTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task id must not be empty");
        }

        // Try to get from cache first
        TaskResponse cachedTask = taskCacheService.getCachedTask(taskId);
        if (cachedTask != null) {
            log.debug("Task {} found in cache", taskId);
            return cachedTask;
        }

        Task task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        TaskResponse response = mapToResponse(task);

        // Cache the task for future requests
        taskCacheService.cacheTask(taskId, response);

        return response;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "taskLists", key = "#boardId", unless = "#result == null")
    public List<TaskResponse> getTasksByBoard(String boardId) {
        String cacheKey = taskCacheService.generateBoardTasksKey(boardId);

        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedTaskList(cacheKey);
        if (cachedTasks != null) {
            log.debug("Tasks for board {} found in cache", boardId);
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findByBoardId(boardId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the task list
        taskCacheService.cacheTaskList(cacheKey, tasks);

        return tasks;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userTasks", key = "#userId", unless = "#result == null")
    public List<TaskResponse> getTasksByAssignee(String userId) {
        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedUserTasks(userId);
        if (cachedTasks != null) {
            log.debug("Tasks for user {} found in cache", userId);
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findByAssignedTo(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the user tasks
        taskCacheService.cacheUserTasks(userId, tasks);

        return tasks;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "taskLists", key = "'overdue'", unless = "#result == null")
    public List<TaskResponse> getOverdueTasks() {
        String cacheKey = taskCacheService.generateOverdueTasksKey();

        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedTaskList(cacheKey);
        if (cachedTasks != null) {
            log.debug("Overdue tasks found in cache");
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findOverdueTasks(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the overdue tasks
        taskCacheService.cacheTaskList(cacheKey, tasks);

        return tasks;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "taskCounts", key = "#boardId + ':' + #status", unless = "#result == null")
    public long getTaskCount(String boardId, TaskStatus status) {
        String cacheKey = taskCacheService.generateTaskCountKey(boardId, status);

        // Try to get from cache first
        Long cachedCount = taskCacheService.getCachedTaskCount(cacheKey);
        if (cachedCount != null) {
            log.debug("Task count for board {} and status {} found in cache: {}", boardId, status, cachedCount);
            return cachedCount;
        }

        long count = taskRepository.countByBoardIdAndStatus(boardId, status);

        // Cache the count
        taskCacheService.cacheTaskCount(cacheKey, count);

        return count;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "taskLists", key = "#boardId + ':' + #status", unless = "#result == null")
    public List<TaskResponse> getTasksByBoardAndStatus(String boardId, TaskStatus status) {
        String cacheKey = taskCacheService.generateBoardStatusTasksKey(boardId, status);

        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedTaskList(cacheKey);
        if (cachedTasks != null) {
            log.debug("Tasks for board {} and status {} found in cache", boardId, status);
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findByBoardIdAndStatus(boardId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the task list
        taskCacheService.cacheTaskList(cacheKey, tasks);

        return tasks;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "taskLists", key = "#boardId + ':ordered'", unless = "#result == null")
    public List<TaskResponse> getTasksByBoardOrderByCreatedDate(String boardId) {
        String cacheKey = "task_list:board:" + boardId + ":ordered";

        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedTaskList(cacheKey);
        if (cachedTasks != null) {
            log.debug("Ordered tasks for board {} found in cache", boardId);
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the task list
        taskCacheService.cacheTaskList(cacheKey, tasks);

        return tasks;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userTasks", key = "#userId + ':' + #status", unless = "#result == null")
    public List<TaskResponse> getTasksByAssigneeAndStatus(String userId, TaskStatus status) {
        String cacheKey = "user_task:" + userId + ":" + status;

        // Try to get from cache first
        List<TaskResponse> cachedTasks = taskCacheService.getCachedTaskList(cacheKey);
        if (cachedTasks != null) {
            log.debug("Tasks for user {} and status {} found in cache", userId, status);
            return cachedTasks;
        }

        List<TaskResponse> tasks = taskRepository.findByAssignedToAndStatus(userId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the task list
        taskCacheService.cacheTaskList(cacheKey, tasks);

        return tasks;
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

    // ===== KAFKA EVENT PUBLISHING METHODS =====

    /**
     * Publish task created event
     */
    private void publishTaskCreatedEvent(Task task, TaskCreateRequest request) {
        try {
            TaskCreatedEvent event = TaskCreatedEvent.taskCreatedEventBuilder()
                    .taskId(task.getId().toString())
                    .userId(request.getAssignedTo()) // Could be the creator or assignee
                    .boardId(task.getBoardId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .status(task.getStatus())
                    .priority(task.getPriority())
                    .assignedTo(task.getAssignedTo())
                    .dueDate(task.getDueDate())
                    .build();

            kafkaService.publishTaskEvent(event)
                    .thenRun(() -> log.info("Task created event published for task: {}", task.getId()))
                    .exceptionally(throwable -> {
                        log.error("Failed to publish task created event for task: {}", task.getId(), throwable);
                        return null;
                    });

            // Publish analytics event
            kafkaService.publishAnalytics("task_created", request.getAssignedTo(), task.getBoardId(),
                    Map.of("taskId", task.getId(), "priority", task.getPriority()));

        } catch (Exception e) {
            log.error("Error publishing task created event for task: {}", task.getId(), e);
        }
    }

    /**
     * Publish task updated event
     */
    private void publishTaskUpdatedEvent(Task task, TaskCreateRequest request, String oldAssignee, String oldBoardId) {
        try {
            Map<String, Object> changedFields = new HashMap<>();

            // Track what fields changed
            if (oldAssignee != null && !oldAssignee.equals(request.getAssignedTo())) {
                changedFields.put("assignedTo", Map.of("old", oldAssignee, "new", request.getAssignedTo()));
            }
            if (oldBoardId != null && !oldBoardId.equals(request.getBoardId())) {
                changedFields.put("boardId", Map.of("old", oldBoardId, "new", request.getBoardId()));
            }

            TaskUpdatedEvent event = TaskUpdatedEvent.taskUpdatedEventBuilder()
                    .taskId(task.getId().toString())
                    .userId(request.getAssignedTo())
                    .boardId(task.getBoardId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .status(task.getStatus())
                    .priority(task.getPriority())
                    .assignedTo(task.getAssignedTo())
                    .dueDate(task.getDueDate())
                    .changedFields(changedFields)
                    .build();

            kafkaService.publishTaskEvent(event)
                    .thenRun(() -> log.info("Task updated event published for task: {}", task.getId()))
                    .exceptionally(throwable -> {
                        log.error("Failed to publish task updated event for task: {}", task.getId(), throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error publishing task updated event for task: {}", task.getId(), e);
        }
    }

    /**
     * Publish task status changed event
     */
    private void publishTaskStatusChangedEvent(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        try {
            TaskStatusChangedEvent event = TaskStatusChangedEvent.taskStatusChangedEventBuilder()
                    .taskId(task.getId().toString())
                    .userId(task.getAssignedTo())
                    .boardId(task.getBoardId())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .reason("Status updated via API")
                    .build();

            kafkaService.publishTaskEvent(event)
                    .thenRun(() -> log.info("Task status changed event published for task: {}", task.getId()))
                    .exceptionally(throwable -> {
                        log.error("Failed to publish task status changed event for task: {}", task.getId(), throwable);
                        return null;
                    });

            // Publish notification if task is completed
            if (newStatus == TaskStatus.DONE && task.getAssignedTo() != null) {
                kafkaService.publishNotification(task.getAssignedTo(),
                        "Task '" + task.getTitle() + "' has been completed!", "task_completed");
            }

        } catch (Exception e) {
            log.error("Error publishing task status changed event for task: {}", task.getId(), e);
        }
    }

    /**
     * Publish task deleted event
     */
    private void publishTaskDeletedEvent(Task task) {
        try {
            TaskDeletedEvent event = TaskDeletedEvent.taskDeletedEventBuilder()
                    .taskId(task.getId().toString())
                    .userId(task.getAssignedTo())
                    .boardId(task.getBoardId())
                    .title(task.getTitle())
                    .reason("Task deleted via API")
                    .build();

            kafkaService.publishTaskEvent(event)
                    .thenRun(() -> log.info("Task deleted event published for task: {}", task.getId()))
                    .exceptionally(throwable -> {
                        log.error("Failed to publish task deleted event for task: {}", task.getId(), throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error publishing task deleted event for task: {}", task.getId(), e);
        }
    }

    /**
     * Check for overdue tasks and publish overdue events
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void checkAndPublishOverdueTasks() {
        try {
            List<TaskResponse> overdueTasks = getOverdueTasks();

            for (TaskResponse overdueTask : overdueTasks) {
                TaskOverdueEvent event = TaskOverdueEvent.taskOverdueEventBuilder()
                        .taskId(overdueTask.getId())
                        .userId(overdueTask.getAssignedTo())
                        .boardId(overdueTask.getBoardId())
                        .title(overdueTask.getTitle())
                        .dueDate(overdueTask.getDueDate())
                        .daysOverdue(calculateDaysOverdue(overdueTask.getDueDate()))
                        .build();

                kafkaService.publishTaskEvent(event)
                        .thenRun(() -> log.info("Task overdue event published for task: {}", overdueTask.getId()))
                        .exceptionally(throwable -> {
                            log.error("Failed to publish task overdue event for task: {}", overdueTask.getId(),
                                    throwable);
                            return null;
                        });

                // Publish notification
                if (overdueTask.getAssignedTo() != null) {
                    kafkaService.publishNotification(overdueTask.getAssignedTo(),
                            "Task '" + overdueTask.getTitle() + "' is overdue!", "task_overdue");
                }
            }
        } catch (Exception e) {
            log.error("Error checking and publishing overdue tasks", e);
        }
    }

    /**
     * Calculate days overdue
     */
    private int calculateDaysOverdue(LocalDateTime dueDate) {
        if (dueDate == null)
            return 0;
        return (int) java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
    }
}

// Custom exception for better error handling
class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}