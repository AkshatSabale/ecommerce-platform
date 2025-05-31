package com.ecommerce.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class RedisCacheConfig {

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .disableCachingNullValues()
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    // Define specific cache configurations
    Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
        "users", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // Longer TTL for user list
            .disableCachingNullValues(),

        "user", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // Longer TTL for individual users
            .disableCachingNullValues(),

        "products", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15))
            .disableCachingNullValues(),

        "orders", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(20))
            .disableCachingNullValues()
    );

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(redisCacheConfiguration()) // Default config
        .withInitialCacheConfigurations(cacheConfigurations) // Specific configs
        .transactionAware() // Enable transaction support
        .build();
  }
}