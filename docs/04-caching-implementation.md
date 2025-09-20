Caching Implementation
Overview

An advanced caching system has been implemented using Redis to improve application performance and reduce database load.

Main Components
1. Cache Configuration (CacheConfig.java)

Location: src/main/java/com/company/app/infrastructure/redis/config/CacheConfig.java

Purpose: Configures the Redis Cache Manager with different settings for each data type.

Features:

Different TTL (Time To Live) configurations

JSON serialization for complex data

Redis connection management

2. Cache Service (TaskCacheService.java)

Location: src/main/java/com/company/app/modules/taskCore/infrastructure/cache/TaskCacheService.java

Purpose: Manages task-related caching.

Features:

Store individual tasks

Store task lists

Store task counters

Store user tasks

Cache statistics

3. Performance Monitoring (CacheMetricsService.java)

Location: src/main/java/com/company/app/infrastructure/redis/monitoring/CacheMetricsService.java

Purpose: Collects detailed cache usage statistics.

Features:

Track cache hits and misses

Measure execution times

Operation statistics

Success rate calculations

Cache Types
1. Individual Task Cache

TTL: 1 hour

Usage: Store details of a single task

Key: task:{taskId}

2. Task List Cache

TTL: 30 minutes

Usage: Store task lists (by board, user, status)

Key: task_list:{identifier}

3. Task Counter Cache

TTL: 15 minutes

Usage: Store counts of tasks by different criteria

Key: task_count:{identifier}

4. User Task Cache

TTL: 45 minutes

Usage: Store all tasks assigned to a specific user

Key: user_task:{userId}

Management APIs
1. Cache Management

Base URL: /api/cache

Endpoints:

GET /stats - Retrieve cache statistics

DELETE /clear - Clear all cache

DELETE /task/{taskId} - Remove a specific task

DELETE /board/{boardId} - Remove tasks from a specific board

DELETE /user/{userId} - Remove tasks of a specific user

DELETE /overdue - Remove overdue tasks

2. Performance Monitoring

Base URL: /api/metrics

Endpoints:

GET /cache - Retrieve overall cache statistics

GET /cache/average-times - Get average execution times

POST /cache/reset - Reset statistics

Cache Invalidation Strategy
When creating a new task:

Remove all related task lists for the board

Remove the assigned user’s task cache (if any)

Remove overdue tasks

Add the new task to cache

When updating a task:

Update the task in cache

Remove related task lists for old and new boards

Remove affected user task caches

Remove overdue tasks

When deleting a task:

Remove the task from cache

Remove all related task lists

Remove affected user task caches

Settings
In application.properties:
# Redis Configuration
spring.data.redis.host=${SPRING_REDIS_HOST:task-mangment-redis}
spring.data.redis.port=${SPRING_REDIS_PORT:6379}
spring.data.redis.timeout=2000ms
spring.data.redis.connection-timeout=2000ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.cache-null-values=false
spring.cache.redis.time-to-live=1800000
spring.cache.redis.enable-statistics=true

Monitoring & Statistics
Available Stats:

Number of cache hits

Number of cache misses

Hit ratio (success rate)

Average execution times

Operation count per type

Uptime

Monitoring Usage:
# Get cache statistics
GET /api/metrics/cache

# Get average execution times
GET /api/metrics/cache/average-times

# Get cache stats
GET /api/cache/stats

Best Practices
1. Memory Management:

Use appropriate TTL for each data type

Avoid storing large data for long periods

Regularly monitor memory usage

2. Performance:

Batch similar operations

Use descriptive cache keys

Avoid unnecessary repeated queries

3. Maintenance:

Regularly monitor cache statistics

Clear cache when needed

Adjust TTL settings based on usage

Troubleshooting
Common Issues:

Data loss from cache: Check TTL settings

Slow performance: Review hit/miss statistics

Connection issues: Verify Redis settings

Diagnostic Tools:

Use /api/cache/stats for status monitoring

Use /api/metrics/cache for performance monitoring

Check logs for cache operation details