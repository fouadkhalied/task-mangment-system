package com.company.app.infrastructure.redis.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * تكوين التخزين المؤقت باستخدام Redis
 * يوفر إعدادات مختلفة لأنواع مختلفة من البيانات المخزنة مؤقتاً
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * تكوين Redis Template للاستخدام العام
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // تكوين التسلسل
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * تكوين Cache Manager مع إعدادات مختلفة لكل نوع من البيانات
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfiguration())
                .withCacheConfiguration("tasks", taskCacheConfiguration())
                .withCacheConfiguration("taskLists", taskListCacheConfiguration())
                .withCacheConfiguration("taskCounts", taskCountCacheConfiguration())
                .withCacheConfiguration("userTasks", userTaskCacheConfiguration())
                .build();
    }

    /**
     * التكوين الافتراضي للتخزين المؤقت
     */
    private RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * تكوين تخزين المهام الفردية (TTL: 1 ساعة)
     */
    private RedisCacheConfiguration taskCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * تكوين تخزين قوائم المهام (TTL: 30 دقيقة)
     */
    private RedisCacheConfiguration taskListCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * تكوين تخزين عداد المهام (TTL: 15 دقيقة)
     */
    private RedisCacheConfiguration taskCountCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * تكوين تخزين مهام المستخدم (TTL: 45 دقيقة)
     */
    private RedisCacheConfiguration userTaskCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(45))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
