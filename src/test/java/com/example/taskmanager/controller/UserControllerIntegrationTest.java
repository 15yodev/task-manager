package com.example.taskmanager.controller;

import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // ★ Поднимаем весь контекст Spring и подключаемся к БД
@AutoConfigureMockMvc // ★ Включаем инструмент для имитации HTTP-запросов
@Transactional // ★ Важно: откатывает все изменения в БД после теста!
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Имитация клиента (как Postman, только в коде)

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_thenGetUser_shouldWorkEndToEnd() throws Exception {
        // 1. Подготовка JSON (как в Postman)
        String requestJson = """
                {
                    "name": "Integration Test",
                    "email": "integration@test.com"
                }
                """;

        // 2. Отправляем POST запрос и проверяем ответ
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated()) // Ожидаем 201
                .andExpect(jsonPath("$.name").value("Integration Test"))
                .andExpect(jsonPath("$.id").exists()); // Проверяем, что БД выдала ID

        // 3. Проверяем, что данные реально сохранились в БД
        assertTrue(userRepository.existsByEmail("integration@test.com"));

        // 4. Отправляем GET запрос, чтобы найти этого пользователя
        // (Для простоты ищем всех, так как мы не знаем точный id, хотя jsonPath его вернул)
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'integration@test.com')]").exists());
    }
}