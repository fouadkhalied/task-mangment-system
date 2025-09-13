package com.company.app.presentation.controller;

import com.company.app.application.service.TaskApplicationService;
import com.company.app.application.dto.TaskCreateRequest;
import com.company.app.application.dto.TaskResponse;
import com.company.app.domain.valueobject.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskApplicationService taskService;
    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        log.info("Creating new task: {}", request.getTitle());
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<TaskResponse>> getTasksByBoard(@PathVariable String boardId) {
        log.info("Fetching tasks for board: {}", boardId);
        List<TaskResponse> tasks = taskService.getTasksByBoard(boardId);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String taskId, 
            @RequestParam TaskStatus status) {
        log.info("Updating task {} status to {}", taskId, status);
        TaskResponse response = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        log.info("Fetching overdue tasks");
        List<TaskResponse> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task service is running!");
    }
}

