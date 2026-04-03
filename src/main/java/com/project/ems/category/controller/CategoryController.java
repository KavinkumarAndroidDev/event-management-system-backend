package com.project.ems.category.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.category.dto.CategoryCreateRequest;
import com.project.ems.category.dto.CategoryStatusRequest;
import com.project.ems.category.dto.CategoryUpdateRequest;
import com.project.ems.category.service.CategoryService;
import com.project.ems.common.entity.Category;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("http://localhost:5173/")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private String resolveRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities() == null) {
            return "PUBLIC";
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("PUBLIC");
    }

    // GET /categories — PUBLIC sees ACTIVE only; ADMIN sees all
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listActiveCategories() {
        String role = resolveRole();
        List<Category> categories = categoryService.listCategories(role);
        return ResponseEntity.ok(categories);
    }

    // GET /categories/{id} — PUBLIC sees ACTIVE only; ADMIN sees all
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        String role = resolveRole();
        Category category = categoryService.getCategoryById(id, role);
        return ResponseEntity.ok(category);
    }

    // POST /categories — ADMIN only
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    // PUT /categories/{id} — ADMIN only
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryUpdateRequest request) {
        Category category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    // PATCH /categories/{id}/status — ADMIN only
    @PatchMapping("/categories/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategoryStatus(@PathVariable Long id,
                                                         @Valid @RequestBody CategoryStatusRequest request) {
        Category category = categoryService.updateCategoryStatus(id, request);
        return ResponseEntity.ok(category);
    }
}
