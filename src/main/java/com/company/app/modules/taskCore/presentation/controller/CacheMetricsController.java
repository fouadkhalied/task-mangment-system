package com.company.app.modules.taskCore.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.app.infrastructure.redis.monitoring.CacheMetricsService;
import com.company.app.infrastructure.redis.monitoring.CacheMetricsService.CacheMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * وحدة تحكم لمراقبة أداء التخزين المؤقت
 * توفر إحصائيات مفصلة حول استخدام التخزين المؤقت
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CacheMetricsController {

    private final CacheMetricsService cacheMetricsService;

    /**
     * الحصول على إحصائيات التخزين المؤقت الشاملة
     */
    @GetMapping("/cache")
    public ResponseEntity<CacheMetrics> getCacheMetrics() {
        log.info("Getting cache performance metrics");
        CacheMetrics metrics = cacheMetricsService.getCacheMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * الحصول على متوسط أوقات التنفيذ
     */
    @GetMapping("/cache/average-times")
    public ResponseEntity<Map<String, Double>> getAverageOperationTimes() {
        log.info("Getting average operation times");
        Map<String, Double> averages = cacheMetricsService.getAverageOperationTimes();
        return ResponseEntity.ok(averages);
    }

    /**
     * إعادة تعيين إحصائيات التخزين المؤقت
     */
    @PostMapping("/cache/reset")
    public ResponseEntity<String> resetCacheMetrics() {
        log.warn("Resetting cache metrics by admin request");
        cacheMetricsService.resetMetrics();
        return ResponseEntity.ok("تم إعادة تعيين إحصائيات التخزين المؤقت");
    }

    /**
     * واجهة للتحقق من صحة المراقبة
     */
    @GetMapping("/health")
    public ResponseEntity<String> metricsHealth() {
        return ResponseEntity.ok("خدمة مراقبة الأداء تعمل بشكل طبيعي");
    }
}
