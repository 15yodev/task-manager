package com.example.taskmanager.cache;

import com.example.taskmanager.dto.TaskResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
// Работает, если enabled=true
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class RedisCacheStrategy implements TaskCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "task:";

    @Override
    public Optional<TaskResponseDTO> get(Long id) {
        log.info("Redis Strategy: ищем в кэше {}", id);
        return Optional.ofNullable((TaskResponseDTO) redisTemplate.opsForValue().get(PREFIX + id));
    }

    @Override
    public void put(Long id, TaskResponseDTO task) {
        log.info("Redis Strategy: кладем в кэш {}", id);
        redisTemplate.opsForValue().set(PREFIX + id, task);
    }

    @Override
    public void evict(Long id) {
        log.info("Redis Strategy: удаляем из кэша {}", id);
        redisTemplate.delete(PREFIX + id);
    }
}