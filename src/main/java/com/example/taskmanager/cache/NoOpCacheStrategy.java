package com.example.taskmanager.cache;

import com.example.taskmanager.dto.TaskResponseDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
// Работает, если enabled=false
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpCacheStrategy implements TaskCacheService {

    @Override
    public Optional<TaskResponseDTO> get(Long id) {
        return Optional.empty(); // Всегда пусто (идем в БД)
    }

    @Override
    public void put(Long id, TaskResponseDTO task) {
        // Ничего не делаем
    }

    @Override
    public void evict(Long id) {
        // Ничего
    }
}