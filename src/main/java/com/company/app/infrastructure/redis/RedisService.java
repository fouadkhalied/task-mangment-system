package com.company.app.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // ===== 1. CACHING - Most Common Use Case =====

    public void cacheUserData(String userId, String userData) {
        // Cache user data for 1 hour
        redisTemplate.opsForValue().set("user:" + userId, userData, Duration.ofHours(1));
    }

    public String getCachedUserData(String userId) {
        return redisTemplate.opsForValue().get("user:" + userId);
    }

    // ===== 2. SESSION STORAGE =====

    public void storeSession(String sessionId, String userInfo) {
        // Store session for 24 hours
        redisTemplate.opsForValue().set("session:" + sessionId, userInfo, Duration.ofHours(24));
    }

    public String getSession(String sessionId) {
        return redisTemplate.opsForValue().get("session:" + sessionId);
    }

    public void invalidateSession(String sessionId) {
        redisTemplate.delete("session:" + sessionId);
    }

    // ===== 3. COUNTERS & STATISTICS =====

    public Long incrementPageViews(String pageId) {
        return redisTemplate.opsForValue().increment("page_views:" + pageId);
    }

    public Long getPageViews(String pageId) {
        String views = redisTemplate.opsForValue().get("page_views:" + pageId);
        return views != null ? Long.parseLong(views) : 0L;
    }

    // ===== 4. TEMPORARY DATA STORAGE =====

    public void storeTemporaryCode(String email, String verificationCode) {
        // Store verification code for 5 minutes
        redisTemplate.opsForValue().set(
                "verify:" + email,
                verificationCode,
                Duration.ofMinutes(5));
    }

    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get("verify:" + email);
    }

    // ===== 5. LISTS - For Queues, Recent Items =====

    public void addToRecentActivity(String userId, String activity) {
        String key = "recent:" + userId;
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        // Add to front of list
        listOps.leftPush(key, activity);

        // Keep only last 10 activities
        listOps.trim(key, 0, 9);

        // Set expiration
        redisTemplate.expire(key, Duration.ofDays(7));
    }

    public List<String> getRecentActivity(String userId) {
        return redisTemplate.opsForList().range("recent:" + userId, 0, -1);
    }

    // ===== 6. SETS - For Unique Collections =====

    public void addToFavorites(String userId, String itemId) {
        redisTemplate.opsForSet().add("favorites:" + userId, itemId);
    }

    public void removeFromFavorites(String userId, String itemId) {
        redisTemplate.opsForSet().remove("favorites:" + userId, itemId);
    }

    public Set<String> getFavorites(String userId) {
        return redisTemplate.opsForSet().members("favorites:" + userId);
    }

    public Boolean isFavorite(String userId, String itemId) {
        return redisTemplate.opsForSet().isMember("favorites:" + userId, itemId);
    }

    // ===== 7. HASH MAPS - For Complex Objects =====

    public void storeUserProfile(String userId, Map<String, String> profile) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.putAll("profile:" + userId, new java.util.HashMap<>(profile));
        redisTemplate.expire("profile:" + userId, Duration.ofHours(6));
    }

    public Map<String, String> getUserProfile(String userId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries("profile:" + userId);
        return raw.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
    }

    public void updateProfileField(String userId, String field, String value) {
        redisTemplate.opsForHash().put("profile:" + userId, field, value);
    }

    // ===== 8. RATE LIMITING =====

    public boolean isRateLimited(String userId, int maxRequests, Duration window) {
        String key = "rate_limit:" + userId;

        // Get current count
        String currentStr = redisTemplate.opsForValue().get(key);
        int current = currentStr != null ? Integer.parseInt(currentStr) : 0;

        if (current >= maxRequests) {
            return true; // Rate limited
        }

        // Increment counter
        if (current == 0) {
            // First request, set with expiration
            redisTemplate.opsForValue().set(key, "1", window);
        } else {
            // Increment existing counter
            redisTemplate.opsForValue().increment(key);
        }

        return false; // Not rate limited
    }

    // ===== 9. PUB/SUB MESSAGING =====

    public void publishNotification(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    // ===== 10. UTILITY METHODS =====

    public void clearCache(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public void setExpiration(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }
}