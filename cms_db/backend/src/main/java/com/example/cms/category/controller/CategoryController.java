package com.example.cms.category.controller;

import com.example.cms.category.dto.CategoryRequest;
import com.example.cms.category.dto.CategoryResponse;
import com.example.cms.category.service.CategoryService;
import com.example.cms.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分類端點（FR-03）。
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> tree() {
        return ApiResponse.success(categoryService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<CategoryResponse> update(@PathVariable Integer id,
                                                @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<Void> delete(@PathVariable Integer id,
                                    @RequestParam(required = false) Integer moveToCategoryId) {
        categoryService.delete(id, moveToCategoryId);
        return ApiResponse.success();
    }
}