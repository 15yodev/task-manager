package com.example.taskmanager.service;

import com.example.taskmanager.cache.TaskCacheService;
import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.event.TaskCreatedEvent;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;
    private final TaskEventProducer taskEventProducer;
    private final TaskCacheService taskCacheService; // ★ Добавили стратегию

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository,
                       TaskMapper taskMapper,
                       TaskEventProducer taskEventProducer,
                       TaskCacheService taskCacheService) { // ★ Внедрили
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.taskMapper = taskMapper;
        this.taskEventProducer = taskEventProducer;
        this.taskCacheService = taskCacheService;
    }

    // ★ Оставляем Spring Cache для списка всех задач ★
    @Cacheable(value = "tasks", key = "'all'")
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        log.info("Fetching ALL tasks from DB");
        return taskRepository.findAll().stream().map(taskMapper::toResponse).toList();
    }

    // ★ Убрали @Cacheable, теперь здесь работает Паттерн Стратегия ★
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        return taskCacheService.get(id).orElseGet(() -> {
            log.info("Fetching task from DB: id={}", id);
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

            TaskResponseDTO dto = taskMapper.toResponse(task);
            taskCacheService.put(id, dto); // Сохраняем в ручной кэш
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category with id " + categoryId + " not found");
        }
        return taskRepository.findByCategoryId(categoryId).stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getFilteredTasks(TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<Task> tasksPage = taskRepository.findTasksByStatusAndCreatedAtBetween(status, from, to, pageable);
        return tasksPage.map(taskMapper::toResponse);
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        log.info("Creating task: title: {}, userId: {}, categoryId: {}", dto.title(), dto.userId(), dto.categoryId());
        User user = userRepository.findById(dto.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status() != null ? dto.status() : TaskStatus.TODO);
        task.setUser(user);
        task.setCategory(category);

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully: id={}", savedTask.getId());

        var event = new TaskCreatedEvent(savedTask.getId(), savedTask.getTitle(), user.getId());
        taskEventProducer.sendTaskCreatedEvent(event);

        return taskMapper.toResponse(savedTask);
    }

    // ★ Чистим ОБА кэша при обновлении ★
    // 1. @CacheEvict(allEntries = true) чистит кэш "tasks" (для getAllTasks)
    // 2. taskCacheService.evict(id) чистит ручной кэш (для getTaskById)
    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO dto) {
        log.info("Updating task id: {}", id);
        Task existing = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        existing.setTitle(dto.title());
        existing.setDescription(dto.description());
        if (dto.status() != null) {
            existing.setStatus(dto.status());
        }

        Task saved = taskRepository.save(existing);

        taskCacheService.evict(id); // Чистим ручной кэш!

        log.info("Task updated, id: {}", id);
        return taskMapper.toResponse(saved);
    }

    // ★ Чистим ОБА кэша при удалении ★
    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);

        taskCacheService.evict(id); // Чистим ручной кэш!

        log.info("Deleted task with id: {}", id);
    }
}