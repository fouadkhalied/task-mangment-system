package com.company.app.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.company.app.modules.taskCore.application.dto.TaskResponse;
import com.company.app.modules.taskCore.domain.valueobject.Priority;
import com.company.app.modules.taskCore.domain.valueobject.TaskStatus;
import com.company.app.modules.taskCore.infrastructure.cache.TaskCacheService;

/**
 * Cache Integration Tests
 * Tests the caching functionality with Redis
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CacheIntegrationTest {

    @Autowired
    private TaskCacheService taskCacheService;

    @Test
    void testTaskCaching() {
        // Create test task
        TaskResponse testTask = TaskResponse.builder()
                .id("test-task-1")
                .title("Test Task")
                .description("Test task description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .assignedTo("user-1")
                .boardId("board-1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .overdue(false)
                .build();

        // Test caching the task
        TaskResponse cachedTask = taskCacheService.cacheTask("test-task-1", testTask);
        assertNotNull(cachedTask);
        assertEquals("test-task-1", cachedTask.getId());

        // Test retrieving task from cache
        TaskResponse retrievedTask = taskCacheService.getCachedTask("test-task-1");
        assertNotNull(retrievedTask);
        assertEquals("Test Task", retrievedTask.getTitle());

        // Test evicting task from cache
        taskCacheService.evictTask("test-task-1");
        TaskResponse evictedTask = taskCacheService.getCachedTask("test-task-1");
        assertNull(evictedTask);
    }

    @Test
    void testTaskListCaching() {
        // Create test task list
        List<TaskResponse> testTasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            testTasks.add(TaskResponse.builder()
                    .id("task-" + i)
                    .title("Task " + i)
                    .status(TaskStatus.TODO)
                    .boardId("board-1")
                    .build());
        }

        String cacheKey = "test-board-tasks";

        // Test caching the task list
        List<TaskResponse> cachedTasks = taskCacheService.cacheTaskList(cacheKey, testTasks);
        assertNotNull(cachedTasks);
        assertEquals(3, cachedTasks.size());

        // Test retrieving task list from cache
        List<TaskResponse> retrievedTasks = taskCacheService.getCachedTaskList(cacheKey);
        assertNotNull(retrievedTasks);
        assertEquals(3, retrievedTasks.size());

        // Test evicting task list from cache
        taskCacheService.evictTaskList(cacheKey);
        List<TaskResponse> evictedTasks = taskCacheService.getCachedTaskList(cacheKey);
        assertNull(evictedTasks);
    }

    @Test
    void testTaskCountCaching() {
        String cacheKey = "test-count";
        Long testCount = 5L;

        // Test caching the count
        Long cachedCount = taskCacheService.cacheTaskCount(cacheKey, testCount);
        assertNotNull(cachedCount);
        assertEquals(5L, cachedCount);

        // Test retrieving count from cache
        Long retrievedCount = taskCacheService.getCachedTaskCount(cacheKey);
        assertNotNull(retrievedCount);
        assertEquals(5L, retrievedCount);

        // Test evicting count from cache
        taskCacheService.evictTaskCount(cacheKey);
        Long evictedCount = taskCacheService.getCachedTaskCount(cacheKey);
        assertNull(evictedCount);
    }

    @Test
    void testUserTaskCaching() {
        // Create test user tasks
        List<TaskResponse> userTasks = new ArrayList<>();
        userTasks.add(TaskResponse.builder()
                .id("user-task-1")
                .title("User Task 1")
                .assignedTo("user-1")
                .build());

        String userId = "user-1";

        // Test caching user tasks
        List<TaskResponse> cachedUserTasks = taskCacheService.cacheUserTasks(userId, userTasks);
        assertNotNull(cachedUserTasks);
        assertEquals(1, cachedUserTasks.size());

        // Test retrieving user tasks from cache
        List<TaskResponse> retrievedUserTasks = taskCacheService.getCachedUserTasks(userId);
        assertNotNull(retrievedUserTasks);
        assertEquals(1, retrievedUserTasks.size());

        // Test evicting user tasks from cache
        taskCacheService.evictUserTasks(userId);
        List<TaskResponse> evictedUserTasks = taskCacheService.getCachedUserTasks(userId);
        assertNull(evictedUserTasks);
    }

    @Test
    void testCacheStats() {
        // Test getting cache statistics
        TaskCacheService.CacheStats stats = taskCacheService.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalCachedItems() >= 0);
    }

    @Test
    void testCacheKeyGeneration() {
        // Test cache key generation
        String boardKey = taskCacheService.generateBoardTasksKey("board-1");
        assertEquals("task_list:board:board-1", boardKey);

        String statusKey = taskCacheService.generateBoardStatusTasksKey("board-1", TaskStatus.TODO);
        assertEquals("task_list:board:board-1:status:TODO", statusKey);

        String countKey = taskCacheService.generateTaskCountKey("board-1", TaskStatus.DONE);
        assertEquals("task_count:board:board-1:status:DONE", countKey);

        String overdueKey = taskCacheService.generateOverdueTasksKey();
        assertEquals("overdue_tasks", overdueKey);
    }
}
