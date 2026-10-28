package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserRequestDTO;
import com.example.taskmanager.dto.UserResponseDTO;
import com.example.taskmanager.exception.ResourceAlreadyExistsException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.UserMapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        // 1. Логируем входящие данные
        log.info("Creating user with email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            throw new ResourceAlreadyExistsException("User with email " + dto.email() + " already exists");
        }
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        log.info("User created successfully: id={}, name={}", savedUser.getId(), savedUser.getName());
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        log.info("Updating User with id: {}", id);
        User existing = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (!existing.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ResourceAlreadyExistsException("User with email " + dto.email() + " already exists");
        }

        existing.setName(dto.name());
        existing.setEmail(dto.email());
        log.info("User updated, id: {}", id);
        return userMapper.toResponse(userRepository.save(existing));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
        log.info("Deleted User with id: {}", id);
    }
}
