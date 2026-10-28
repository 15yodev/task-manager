package com.example.taskmanager.cache;

import com.example.taskmanager.dto.TaskResponseDTO;

import java.util.Optional;

public interface TaskCacheService {
    Optional<TaskResponseDTO> get(Long id);

    void put(Long id, TaskResponseDTO task);

    void evict(Long id);
}