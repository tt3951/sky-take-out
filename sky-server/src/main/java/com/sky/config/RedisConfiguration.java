package com.sky.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

        log.info("开始创建redis模版对象.....");
        RedisTemplate redisTemplate = new RedisTemplate();

        //设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;

    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate1(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // 1. 创建 JSON 序列化工具
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 2. 配置 ObjectMapper (核心步骤)
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置所有访问权限以及所有的修饰符，字段、get、set方法等
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 开启类型信息的存储，这样反序列化时才知道是哪个具体的类
        // (如果不配置这个，从Redis读出来的数据可能只是一个LinkedHashMap，无法转回你的User对象)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        jsonSerializer.setObjectMapper(objectMapper);

        // 3. 创建 String 序列化工具 (用于 Key)
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 4. 设置 Key 和 Value 的序列化方式

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);

        // value序列化方式采用jackson
        template.setValueSerializer(jsonSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
