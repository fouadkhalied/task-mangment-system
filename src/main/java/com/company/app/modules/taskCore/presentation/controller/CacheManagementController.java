package com.company.app.modules.taskCore.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.app.modules.taskCore.infrastructure.cache.TaskCacheService;
import com.company.app.modules.taskCore.infrastructure.cache.TaskCacheService.CacheStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache Management Controller
 * Provides REST APIs for monitoring and managing cache operations
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CacheManagementController {

    private final TaskCacheService taskCacheService;

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<CacheStats> getCacheStats() {
        log.info("Getting cache statistics");
        CacheStats stats = taskCacheService.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear all cache entries
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllCache() {
        log.warn("Clearing all cache by admin request");
        taskCacheService.clearAllCache();
        return ResponseEntity.ok("All cache entries cleared successfully");
    }

    /**
     * Evict specific task from cache
     */
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<String> evictTask(@PathVariable String taskId) {
        log.info("Evicting task from cache: {}", taskId);
        taskCacheService.evictTask(taskId);
        return ResponseEntity.ok("Task evicted from cache: " + taskId);
    }

    /**
     * Evict all tasks related to a specific board from cache
     */
    @DeleteMapping("/board/{boardId}")
    public ResponseEntity<String> evictBoardCache(@PathVariable String boardId) {
        log.info("Evicting board cache: {}", boardId);
        taskCacheService.evictBoardCache(boardId);
        return ResponseEntity.ok("All board tasks evicted from cache: " + boardId);
    }

    /**
     * Evict all tasks related to a specific user from cache
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> evictUserCache(@PathVariable String userId) {
        log.info("Evicting user cache: {}", userId);
        taskCacheService.evictUserTasks(userId);
        return ResponseEntity.ok("All user tasks evicted from cache: " + userId);
    }

    /**
     * Evict overdue tasks from cache
     */
    @DeleteMapping("/overdue")
    public ResponseEntity<String> evictOverdueTasks() {
        log.info("Evicting overdue tasks cache");
        taskCacheService.evictOverdueTasks();
        return ResponseEntity.ok("Overdue tasks evicted from cache");
    }

    /**
     * Evict specific task list from cache
     */
    @DeleteMapping("/list")
    public ResponseEntity<String> evictTaskList(@RequestParam String cacheKey) {
        log.info("Evicting task list from cache: {}", cacheKey);
        taskCacheService.evictTaskList(cacheKey);
        return ResponseEntity.ok("Task list evicted from cache: " + cacheKey);
    }

    /**
     * Evict task count from cache
     */
    @DeleteMapping("/count")
    public ResponseEntity<String> evictTaskCount(@RequestParam String cacheKey) {
        log.info("Evicting task count from cache: {}", cacheKey);
        taskCacheService.evictTaskCount(cacheKey);
        return ResponseEntity.ok("Task count evicted from cache: " + cacheKey);
    }

    /**
     * Cache health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> cacheHealth() {
        return ResponseEntity.ok("Cache service is running normally");
    }
}
