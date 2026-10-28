package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCategoryId(Long categoryId);

    List<Task> findByStatus(TaskStatus status);

    // Параметры могут быть null, поэтому пишем (:status IS NULL OR t.status = :status) — это делает фильтр опциональным
    @Query("""
        SELECT t FROM Task t
        WHERE (:status IS NULL OR t.status = :status)
        AND (CAST(:from AS timestamp) IS NULL OR t.createdAt >= :from)
        AND (CAST(:to AS timestamp) IS NULL OR t.createdAt <= :to)
        """)
    Page<Task> findTasksByStatusAndCreatedAtBetween(
            @Param("status") TaskStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable // Пагинация
    );
}
