package com.project.ems.category.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.category.dto.CategoryCreateRequest;
import com.project.ems.category.dto.CategoryStatusRequest;
import com.project.ems.category.dto.CategoryUpdateRequest;
import com.project.ems.category.repository.CategoryRepository;
import com.project.ems.common.entity.Category;
import com.project.ems.common.entity.Status;
import com.project.ems.common.exception.CategoryAlreadyExistsException;
import com.project.ems.common.exception.CategoryNotFoundException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /categories — PUBLIC sees ACTIVE only; ADMIN sees all
    @Transactional(readOnly = true)
    public List<Category> listCategories(String role) {
        if ("ADMIN".equals(role)) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByStatus(Status.ACTIVE);
    }

    // GET /categories — PUBLIC (kept for internal use)
    @Transactional(readOnly = true)
    public List<Category> listActiveCategories() {
        return categoryRepository.findByStatus(Status.ACTIVE);
    }

    // GET /categories/{id} — PUBLIC sees ACTIVE only; ADMIN sees all statuses
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id, String role) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!"ADMIN".equals(role) && category.getStatus() != Status.ACTIVE) {
            throw new CategoryNotFoundException("Category not found");
        }
        return category;
    }

    // GET /categories/{id} — PUBLIC (kept for internal use)
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    }

    // POST /categories — ADMIN
    @Transactional
    public Category createCategory(CategoryCreateRequest request) {

        boolean isExists = categoryRepository.findByNameIgnoreCase(request.getCategoryName().trim()).isPresent();

        if (isExists) {
            throw new CategoryAlreadyExistsException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.getCategoryName().trim());
        category.setStatus(Status.ACTIVE);

        return categoryRepository.save(category);
    }

    // PUT /categories/{id} — ADMIN
    @Transactional
    public Category updateCategory(Long id, CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        // check duplicate name (excluding current category)
        categoryRepository.findByNameIgnoreCase(request.getCategoryName().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new CategoryAlreadyExistsException("Category name already in use");
                    }
                });

        category.setName(request.getCategoryName().trim());
        return categoryRepository.save(category);
    }

    // PATCH /categories/{id}/status — ADMIN
    @Transactional
    public Category updateCategoryStatus(Long id, CategoryStatusRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setStatus(request.getStatus());
        return categoryRepository.save(category);
    }
}
