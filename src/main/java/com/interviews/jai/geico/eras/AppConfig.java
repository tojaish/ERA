package com.interviews.jai.geico.eras;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = null;
        try {
            RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration("0.0.0.0", 6379);
            
            jedisConnectionFactory = new JedisConnectionFactory(redisConfiguration);
            jedisConnectionFactory.getPoolConfig().setMaxTotal(50);
            jedisConnectionFactory.getPoolConfig().setMaxIdle(50);
        } catch (Exception e) {
            log.info(e.getMessage(), e.getCause());
        }
        return jedisConnectionFactory;
    }

    @Bean(name = "redisgeostringtemplate")
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    GeoOperations<String, String> geoOperations(@Qualifier(value = "redisgeostringtemplate") RedisTemplate<String, String> template) {
        return template.opsForGeo();
    }
}