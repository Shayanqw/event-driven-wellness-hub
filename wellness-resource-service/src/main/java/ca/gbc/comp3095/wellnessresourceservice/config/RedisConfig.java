package ca.gbc.comp3095.wellnessresourceservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching // REQUIRED: Enables Spring's annotation-driven caching
public class RedisConfig {

    // Spring Boot auto-configures RedisConnectionFactory based on properties
    // We just need to define a RedisTemplate bean if needed, but for basic
    // caching using annotations, this is sufficient.

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Using GenericJackson2JsonRedisSerializer is a good practice for complex objects
        // template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}