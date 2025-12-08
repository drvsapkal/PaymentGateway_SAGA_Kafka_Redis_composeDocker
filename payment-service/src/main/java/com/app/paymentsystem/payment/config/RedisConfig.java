package com.app.paymentsystem.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("redis", 6379);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory conn) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(conn);
        return template;
    }
}