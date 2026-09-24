package com.example.cms.tag.controller;

import com.example.cms.common.ApiResponse;
import com.example.cms.tag.dto.TagRequest;
import com.example.cms.tag.dto.TagResponse;
import com.example.cms.tag.service.TagService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 標籤端點（FR-04）。
 */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<TagResponse>> list() {
        return ApiResponse.success(tagService.list());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ApiResponse.success(tagService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<TagResponse> update(@PathVariable Integer id, @Valid @RequestBody TagRequest request) {
        return ApiResponse.success(tagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        tagService.delete(id);
        return ApiResponse.success();
    }
}