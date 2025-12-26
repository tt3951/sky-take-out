package com.sky.config;


import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfiguration {

/*    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

        log.info("开始创建redis模版对象.....");
        RedisTemplate redisTemplate = new RedisTemplate();

        //设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;

    }*/


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 1. 创建 ObjectMapper 并注册 JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        // 关键步骤：注册 Java 8 日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // (可选) 设置为不将日期写为时间戳，而是 ISO 字符串，看你需求，通常不加这行也能跑
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. 创建 JSON 序列化器，并传入配置好的 ObjectMapper
        // 注意：GenericJackson2JsonRedisSerializer 有一个构造函数可以接收 ObjectMapper
        GenericJackson2JsonRedisSerializer jsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 设置 Key 的序列化器 (String)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 4. 设置 Value 的序列化器 (使用刚才配置好的 JSON 序列化器)
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. 创建 ObjectMapper 并注册 JavaTimeModule (处理 LocalDateTime)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // (可选) 忽略未知的属性，防止报错
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 2. 创建 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 配置 Cache 的序列化规则
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // 设置缓存默认过期时间，例如 60 分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // Key 使用 String 序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)) // Value 使用 JSON 序列化
                .disableCachingNullValues(); // 不缓存 null 值

        // 4. 构建 CacheManager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
