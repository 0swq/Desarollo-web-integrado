package com.autopartes.service.impl;

import com.autopartes.dto.category.CategoryRequest;
import com.autopartes.dto.category.CategoryResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.Category;
import com.autopartes.repository.CategoryRepository;
import com.autopartes.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Category category = Category.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));

        if (!category.getNombre().equalsIgnoreCase(request.getNombre().trim())
                && categoryRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new BusinessException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        category.setNombre(request.getNombre().trim());
        category.setDescripcion(request.getDescripcion());
        if (request.getImagenUrl() != null) {
            category.setImagenUrl(request.getImagenUrl());
        }
        if (request.getActivo() != null) {
            category.setActivo(request.getActivo());
        }

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        // Soft delete para mantener integridad referencial
        category.setActivo(false);
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .nombre(category.getNombre())
                .descripcion(category.getDescripcion())
                .imagenUrl(category.getImagenUrl())
                .activo(category.getActivo())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
