package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt,
        Long userId,
        String userName,
        Long categoryId,
        String categoryName
) {}