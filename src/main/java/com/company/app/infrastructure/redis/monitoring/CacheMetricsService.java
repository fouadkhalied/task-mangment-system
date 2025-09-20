package com.company.app.infrastructure.redis.monitoring;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * خدمة مراقبة أداء التخزين المؤقت
 * تجمع إحصائيات مفصلة حول استخدام التخزين المؤقت
 */
@Service
@Slf4j
public class CacheMetricsService {

    // إحصائيات العدادات
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);
    private final AtomicLong cachePuts = new AtomicLong(0);

    // إحصائيات زمنية
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();

    // إحصائيات بدء التشغيل
    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * تسجيل ضربة ناجحة في التخزين المؤقت
     */
    public void recordCacheHit(String operation) {
        cacheHits.incrementAndGet();
        operationCounts.computeIfAbsent(operation + "_hit", k -> new AtomicLong(0)).incrementAndGet();
        log.debug("Cache hit for operation: {}", operation);
    }

    /**
     * تسجيل فشل في التخزين المؤقت
     */
    public void recordCacheMiss(String operation) {
        cacheMisses.incrementAndGet();
        operationCounts.computeIfAbsent(operation + "_miss", k -> new AtomicLong(0)).incrementAndGet();
        log.debug("Cache miss for operation: {}", operation);
    }

    /**
     * تسجيل إضافة عنصر للتخزين المؤقت
     */
    public void recordCachePut(String operation, long executionTimeMs) {
        cachePuts.incrementAndGet();
        operationCounts.computeIfAbsent(operation + "_put", k -> new AtomicLong(0)).incrementAndGet();
        operationTimes.computeIfAbsent(operation + "_put_time", k -> new AtomicLong(0)).addAndGet(executionTimeMs);
        log.debug("Cache put for operation: {}, time: {}ms", operation, executionTimeMs);
    }

    /**
     * تسجيل إزالة عنصر من التخزين المؤقت
     */
    public void recordCacheEviction(String operation) {
        cacheEvictions.incrementAndGet();
        operationCounts.computeIfAbsent(operation + "_evict", k -> new AtomicLong(0)).incrementAndGet();
        log.debug("Cache eviction for operation: {}", operation);
    }

    /**
     * تسجيل وقت تنفيذ عملية
     */
    public void recordOperationTime(String operation, long executionTimeMs) {
        operationTimes.computeIfAbsent(operation + "_time", k -> new AtomicLong(0)).addAndGet(executionTimeMs);
        operationCounts.computeIfAbsent(operation + "_count", k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * الحصول على إحصائيات التخزين المؤقت
     */
    public CacheMetrics getCacheMetrics() {
        long totalRequests = cacheHits.get() + cacheMisses.get();
        double hitRatio = totalRequests > 0 ? (double) cacheHits.get() / totalRequests : 0.0;

        return CacheMetrics.builder()
                .totalHits(cacheHits.get())
                .totalMisses(cacheMisses.get())
                .totalPuts(cachePuts.get())
                .totalEvictions(cacheEvictions.get())
                .hitRatio(hitRatio)
                .missRatio(1.0 - hitRatio)
                .totalRequests(totalRequests)
                .uptimeHours(getUptimeHours())
                .operationCounts(Map.copyOf(operationCounts))
                .operationTimes(Map.copyOf(operationTimes))
                .build();
    }

    /**
     * حساب متوسط أوقات التنفيذ
     */
    public Map<String, Double> getAverageOperationTimes() {
        Map<String, Double> averages = new ConcurrentHashMap<>();

        operationTimes.forEach((operation, totalTime) -> {
            String countKey = operation.replace("_time", "_count");
            AtomicLong count = operationCounts.get(countKey);

            if (count != null && count.get() > 0) {
                double average = (double) totalTime.get() / count.get();
                averages.put(operation.replace("_time", ""), average);
            }
        });

        return averages;
    }

    /**
     * إعادة تعيين الإحصائيات
     */
    public void resetMetrics() {
        log.warn("Resetting cache metrics");
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        cachePuts.set(0);
        operationCounts.clear();
        operationTimes.clear();
    }

    /**
     * الحصول على وقت التشغيل بالساعات
     */
    private double getUptimeHours() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes() / 60.0;
    }

    /**
     * فئة لحفظ إحصائيات التخزين المؤقت
     */
    @lombok.Builder
    @lombok.Data
    public static class CacheMetrics {
        private long totalHits;
        private long totalMisses;
        private long totalPuts;
        private long totalEvictions;
        private double hitRatio;
        private double missRatio;
        private long totalRequests;
        private double uptimeHours;
        private Map<String, AtomicLong> operationCounts;
        private Map<String, AtomicLong> operationTimes;
    }
}
