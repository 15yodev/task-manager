package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequestDTO dto) {
        Category c = new Category();
        c.setName(dto.name());
        return c;
    }

    public CategoryResponseDTO toResponse(Category c) {
        return new CategoryResponseDTO(c.getId(), c.getName());
    }
}
