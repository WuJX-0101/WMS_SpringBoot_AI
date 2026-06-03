package com.wms.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 缓存配置类
 *
 * 功能：启用Spring Cache并配置Redis作为缓存实现
 * 缓存策略：
 * - 仪表盘统计：缓存5分钟
 * - 订单统计：缓存5分钟
 * - 基础数据：缓存10分钟
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String CACHE_DASHBOARD = "dashboard";
    public static final String CACHE_ORDER_STATS = "orderStats";
    public static final String CACHE_PRODUCT = "product";
    public static final String CACHE_WAREHOUSE = "warehouse";
    public static final String CACHE_LOCATION = "location";
    public static final String CACHE_CATEGORY = "category";
    public static final String CACHE_SUPPLIER = "supplier";
    public static final String CACHE_CUSTOMER = "customer";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置ObjectMapper支持LocalDateTime序列化 + 类型信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // 默认缓存10分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 统计数据缓存5分钟
                .withCacheConfiguration(CACHE_DASHBOARD,
                        config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration(CACHE_ORDER_STATS,
                        config.entryTtl(Duration.ofMinutes(5)))
                // 基础数据缓存10分钟（使用默认配置即可）
                .withCacheConfiguration(CACHE_PRODUCT, config)
                .withCacheConfiguration(CACHE_WAREHOUSE, config)
                .withCacheConfiguration(CACHE_LOCATION, config)
                .withCacheConfiguration(CACHE_CATEGORY, config)
                .withCacheConfiguration(CACHE_SUPPLIER, config)
                .withCacheConfiguration(CACHE_CUSTOMER, config)
                .build();
    }
}
