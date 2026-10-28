package com.example.taskmanager.event;

// record (Java 21) — неизменяемый класс. Идеально для событий.
// Мы шлем только ID и заголовок, чтобы не передавать тяжелые связи сущности.
public record TaskCreatedEvent(Long taskId, String title, Long userId) {}