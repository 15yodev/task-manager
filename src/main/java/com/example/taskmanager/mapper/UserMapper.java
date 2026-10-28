package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.UserRequestDTO;
import com.example.taskmanager.dto.UserResponseDTO;
import com.example.taskmanager.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        return user;
    }

    public UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}