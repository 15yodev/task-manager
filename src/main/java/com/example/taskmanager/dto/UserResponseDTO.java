package com.example.taskmanager.dto;

public record UserResponseDTO(
        Long id,
        String name,
        String email
) {
}