package com.example.taskmanager.service;

import com.example.taskmanager.cache.TaskCacheService; // ★ Добавили импорт
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskCacheService taskCacheService; // ★ ВОТ НОВЫЙ МОК

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTaskById_whenTaskExists_shouldReturnTask() {
        Long taskId = 1L;
        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setTitle("Test Task");

        TaskResponseDTO mockResponse = new TaskResponseDTO(taskId, "Test Task", null, null, null, null, null, null, null);

        // ★ ГОВОРИМ КЭШУ, ЧТО ТАМ ПУСТО (чтобы метод пошел в репозиторий)
        when(taskCacheService.get(taskId)).thenReturn(Optional.empty());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));
        when(taskMapper.toResponse(mockTask)).thenReturn(mockResponse);

        TaskResponseDTO result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals("Test Task", result.title());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTaskById_whenTaskNotFound_shouldThrowException() {
        Long taskId = 999L;

        // ★ ТУТ ТОЖЕ ГОВОРИМ КЭШУ, ЧТО ТАМ ПУСТО
        when(taskCacheService.get(taskId)).thenReturn(Optional.empty());

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(taskId)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }
}