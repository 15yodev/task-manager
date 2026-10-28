package com.example.taskmanager.service;

import com.example.taskmanager.event.TaskCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service // Регистрируем как Spring-бин, чтобы внедрить в TaskService
public class TaskEventProducer {

    // KafkaTemplate — это высокоуровневая обертка Spring для отправки сообщений
    private final KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;

    // Имя топика (папки, куда складываются события)
    private static final String TOPIC = "task-created-topic";

    // Конструктор (Spring сам подставит сюда готовый KafkaTemplate)
    public TaskEventProducer(KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTaskCreatedEvent(TaskCreatedEvent event) {
        // Асинхронная отправка: кладем сообщение в буфер и сразу идем дальше
        // Первый аргумент - топик, второй - сам объект события (Jackson превратит его в JSON)
        kafkaTemplate.send(TOPIC, event);
    }
}