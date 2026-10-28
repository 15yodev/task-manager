package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        TaskStatus status,           // может быть null → дефолт TODO

        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "categoryId is required")
        Long categoryId
) {}