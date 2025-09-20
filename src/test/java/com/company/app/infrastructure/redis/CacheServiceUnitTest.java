package com.company.app.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.company.app.modules.taskCore.infrastructure.cache.TaskCacheService;

/**
 * Unit tests for cache service (without Redis dependency)
 * Tests cache key generation and basic functionality
 */
class CacheServiceUnitTest {

    private final TaskCacheService taskCacheService = new TaskCacheService(null);

    @Test
    void testCacheKeyGeneration() {
        // Test cache key generation methods
        String boardKey = taskCacheService.generateBoardTasksKey("board-123");
        assertEquals("task_list:board:board-123", boardKey);

        String statusKey = taskCacheService.generateBoardStatusTasksKey("board-456",
                com.company.app.modules.taskCore.domain.valueobject.TaskStatus.TODO);
        assertEquals("task_list:board:board-456:status:TODO", statusKey);

        String countKey = taskCacheService.generateTaskCountKey("board-789",
                com.company.app.modules.taskCore.domain.valueobject.TaskStatus.DONE);
        assertEquals("task_count:board:board-789:status:DONE", countKey);

        String overdueKey = taskCacheService.generateOverdueTasksKey();
        assertEquals("overdue_tasks", overdueKey);
    }

    @Test
    void testCacheStatsStructure() {
        // Test that cache stats can be created
        TaskCacheService.CacheStats stats = TaskCacheService.CacheStats.builder()
                .cachedTasks(10)
                .cachedTaskLists(5)
                .cachedTaskCounts(3)
                .cachedUserTasks(8)
                .totalCachedItems(26)
                .build();

        assertNotNull(stats);
        assertEquals(10, stats.getCachedTasks());
        assertEquals(5, stats.getCachedTaskLists());
        assertEquals(3, stats.getCachedTaskCounts());
        assertEquals(8, stats.getCachedUserTasks());
        assertEquals(26, stats.getTotalCachedItems());
    }
}
