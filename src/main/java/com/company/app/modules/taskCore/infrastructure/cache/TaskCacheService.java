package com.company.app.modules.taskCore.infrastructure.cache;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.company.app.modules.taskCore.application.dto.TaskResponse;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * خدمة التخزين المؤقت للمهام
 * توفر طرق متخصصة لإدارة تخزين المهام ومعلوماتها مؤقتاً
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ===== CACHE KEYS PATTERNS =====
    private static final String TASK_KEY_PREFIX = "task:";
    private static final String TASK_LIST_KEY_PREFIX = "task_list:";
    private static final String TASK_COUNT_KEY_PREFIX = "task_count:";
    private static final String USER_TASK_KEY_PREFIX = "user_task:";
    private static final String OVERDUE_TASK_KEY = "overdue_tasks";

    // ===== INDIVIDUAL TASK CACHING =====

    /**
     * تخزين مهمة فردية مؤقتاً
     */
    @CachePut(value = "tasks", key = "#taskId")
    public TaskResponse cacheTask(String taskId, TaskResponse task) {
        log.debug("Caching task: {}", taskId);
        return task;
    }

    /**
     * استرجاع مهمة من التخزين المؤقت
     */
    @Cacheable(value = "tasks", key = "#taskId", unless = "#result == null")
    public TaskResponse getCachedTask(String taskId) {
        log.debug("Task not found in cache: {}", taskId);
        return null; // سيتم استرجاعها من قاعدة البيانات
    }

    /**
     * إزالة مهمة من التخزين المؤقت
     */
    @CacheEvict(value = "tasks", key = "#taskId")
    public void evictTask(String taskId) {
        log.debug("Evicting task from cache: {}", taskId);
    }

    // ===== TASK LISTS CACHING =====

    /**
     * تخزين قائمة المهام مؤقتاً
     */
    @CachePut(value = "taskLists", key = "#cacheKey")
    public List<TaskResponse> cacheTaskList(String cacheKey, List<TaskResponse> tasks) {
        log.debug("Caching task list with key: {}, size: {}", cacheKey, tasks.size());
        return tasks;
    }

    /**
     * استرجاع قائمة المهام من التخزين المؤقت
     */
    @Cacheable(value = "taskLists", key = "#cacheKey", unless = "#result == null")
    public List<TaskResponse> getCachedTaskList(String cacheKey) {
        log.debug("Task list not found in cache: {}", cacheKey);
        return null;
    }

    /**
     * إزالة قائمة المهام من التخزين المؤقت
     */
    @CacheEvict(value = "taskLists", key = "#cacheKey")
    public void evictTaskList(String cacheKey) {
        log.debug("Evicting task list from cache: {}", cacheKey);
    }

    // ===== TASK COUNTS CACHING =====

    /**
     * تخزين عدد المهام مؤقتاً
     */
    @CachePut(value = "taskCounts", key = "#cacheKey")
    public Long cacheTaskCount(String cacheKey, Long count) {
        log.debug("Caching task count with key: {}, count: {}", cacheKey, count);
        return count;
    }

    /**
     * استرجاع عدد المهام من التخزين المؤقت
     */
    @Cacheable(value = "taskCounts", key = "#cacheKey", unless = "#result == null")
    public Long getCachedTaskCount(String cacheKey) {
        log.debug("Task count not found in cache: {}", cacheKey);
        return null;
    }

    // ===== USER TASKS CACHING =====

    /**
     * تخزين مهام المستخدم مؤقتاً
     */
    @CachePut(value = "userTasks", key = "#userId")
    public List<TaskResponse> cacheUserTasks(String userId, List<TaskResponse> tasks) {
        log.debug("Caching user tasks for user: {}, size: {}", userId, tasks.size());
        return tasks;
    }

    /**
     * استرجاع مهام المستخدم من التخزين المؤقت
     */
    @Cacheable(value = "userTasks", key = "#userId", unless = "#result == null")
    public List<TaskResponse> getCachedUserTasks(String userId) {
        log.debug("User tasks not found in cache: {}", userId);
        return null;
    }

    /**
     * إزالة مهام المستخدم من التخزين المؤقت
     */
    @CacheEvict(value = "userTasks", key = "#userId")
    public void evictUserTasks(String userId) {
        log.debug("Evicting user tasks from cache: {}", userId);
    }

    // ===== CACHE KEY GENERATORS =====

    /**
     * توليد مفتاح التخزين المؤقت للمهام حسب اللوحة
     */
    public String generateBoardTasksKey(String boardId) {
        return TASK_LIST_KEY_PREFIX + "board:" + boardId;
    }

    /**
     * توليد مفتاح التخزين المؤقت للمهام حسب اللوحة والحالة
     */
    public String generateBoardStatusTasksKey(String boardId, TaskStatus status) {
        return TASK_LIST_KEY_PREFIX + "board:" + boardId + ":status:" + status;
    }

    /**
     * توليد مفتاح التخزين المؤقت لعدد المهام
     */
    public String generateTaskCountKey(String boardId, TaskStatus status) {
        return TASK_COUNT_KEY_PREFIX + "board:" + boardId + ":status:" + status;
    }

    /**
     * توليد مفتاح التخزين المؤقت للمهام المتأخرة
     */
    public String generateOverdueTasksKey() {
        return OVERDUE_TASK_KEY;
    }

    // ===== ADVANCED CACHE OPERATIONS =====

    /**
     * إزالة جميع المفاتيح المتعلقة بلوحة معينة
     */
    public void evictBoardCache(String boardId) {
        log.info("Evicting all cache for board: {}", boardId);

        // إزالة قوائم المهام
        evictTaskList(generateBoardTasksKey(boardId));

        // إزالة المهام حسب الحالة
        for (TaskStatus status : TaskStatus.values()) {
            evictTaskList(generateBoardStatusTasksKey(boardId, status));
            evictTaskCount(generateTaskCountKey(boardId, status));
        }
    }

    /**
     * إزالة عدد المهام من التخزين المؤقت
     */
    @CacheEvict(value = "taskCounts", key = "#cacheKey")
    public void evictTaskCount(String cacheKey) {
        log.debug("Evicting task count from cache: {}", cacheKey);
    }

    /**
     * إزالة جميع المفاتيح المتعلقة بمستخدم معين
     */
    public void evictUserCache(String userId) {
        log.info("Evicting all cache for user: {}", userId);
        evictUserTasks(userId);
    }

    /**
     * إزالة المهام المتأخرة من التخزين المؤقت
     */
    @CacheEvict(value = "taskLists", key = "#root.target.generateOverdueTasksKey()")
    public void evictOverdueTasks() {
        log.debug("Evicting overdue tasks from cache");
    }

    // ===== CACHE STATISTICS =====

    /**
     * الحصول على إحصائيات التخزين المؤقت
     */
    public CacheStats getCacheStats() {
        Set<String> taskKeys = redisTemplate.keys(TASK_KEY_PREFIX + "*");
        Set<String> taskListKeys = redisTemplate.keys(TASK_LIST_KEY_PREFIX + "*");
        Set<String> taskCountKeys = redisTemplate.keys(TASK_COUNT_KEY_PREFIX + "*");
        Set<String> userTaskKeys = redisTemplate.keys(USER_TASK_KEY_PREFIX + "*");

        return CacheStats.builder()
                .cachedTasks(taskKeys != null ? taskKeys.size() : 0)
                .cachedTaskLists(taskListKeys != null ? taskListKeys.size() : 0)
                .cachedTaskCounts(taskCountKeys != null ? taskCountKeys.size() : 0)
                .cachedUserTasks(userTaskKeys != null ? userTaskKeys.size() : 0)
                .totalCachedItems(
                        (taskKeys != null ? taskKeys.size() : 0) +
                                (taskListKeys != null ? taskListKeys.size() : 0) +
                                (taskCountKeys != null ? taskCountKeys.size() : 0) +
                                (userTaskKeys != null ? userTaskKeys.size() : 0))
                .build();
    }

    /**
     * تنظيف التخزين المؤقت بالكامل
     */
    public void clearAllCache() {
        log.warn("Clearing all task cache");

        Set<String> taskKeys = redisTemplate.keys(TASK_KEY_PREFIX + "*");
        Set<String> taskListKeys = redisTemplate.keys(TASK_LIST_KEY_PREFIX + "*");
        Set<String> taskCountKeys = redisTemplate.keys(TASK_COUNT_KEY_PREFIX + "*");
        Set<String> userTaskKeys = redisTemplate.keys(USER_TASK_KEY_PREFIX + "*");
        Set<String> overdueKeys = redisTemplate.keys(OVERDUE_TASK_KEY + "*");

        if (taskKeys != null)
            redisTemplate.delete(taskKeys);
        if (taskListKeys != null)
            redisTemplate.delete(taskListKeys);
        if (taskCountKeys != null)
            redisTemplate.delete(taskCountKeys);
        if (userTaskKeys != null)
            redisTemplate.delete(userTaskKeys);
        if (overdueKeys != null)
            redisTemplate.delete(overdueKeys);
    }

    // ===== INNER CLASSES =====

    /**
     * فئة لحفظ إحصائيات التخزين المؤقت
     */
    @lombok.Builder
    @lombok.Data
    public static class CacheStats {
        private int cachedTasks;
        private int cachedTaskLists;
        private int cachedTaskCounts;
        private int cachedUserTasks;
        private int totalCachedItems;
    }
}
