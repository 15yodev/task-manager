package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.exception.ResourceAlreadyExistsException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.CategoryMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        log.info("Creating categoty: name: {}", dto.name());
        if (categoryRepository.existsByName(dto.name())) {
            throw new ResourceAlreadyExistsException("Category with name " + dto.name() + " already exists");
        }
        Category category = categoryMapper.toEntity(dto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully: id={}, name={}", savedCategory.getId(), savedCategory.getName());
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        log.info("Updating category with id: {}", id);
        Category existing = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));

        if (!existing.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            throw new ResourceAlreadyExistsException("Category with name " + dto.name() + " already exists");
        }

        existing.setName(dto.name());
        log.info("Category updated, id: {}", id);
        return categoryMapper.toResponse(categoryRepository.save(existing));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category with id " + id + " not found");
        }
        categoryRepository.deleteById(id);
        log.info("Deleted Category with id: {}", id);
    }
}