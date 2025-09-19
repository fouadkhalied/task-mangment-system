package com.company.app.modules.taskCore.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.app.modules.taskCore.application.dto.TaskCreateRequest;
import com.company.app.modules.taskCore.application.dto.TaskResponse;
import com.company.app.modules.taskCore.application.service.TaskApplicationService;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String taskId) {
        log.info("Fetching task by id: {}", taskId);
        TaskResponse response = taskService.getTask(taskId);
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
