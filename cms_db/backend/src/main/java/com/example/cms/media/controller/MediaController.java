package com.example.cms.media.controller;

import com.example.cms.common.ApiResponse;
import com.example.cms.common.PageResult;
import com.example.cms.media.dto.MediaResponse;
import com.example.cms.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 多媒體管理端點（FR-06）。
 */
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('admin','editor','author')")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping
    public ApiResponse<MediaResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(mediaService.upload(file));
    }

    @GetMapping
    public ApiResponse<PageResult<MediaResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(mediaService.list(pageable)));
    }

    @PutMapping("/{id}")
    public ApiResponse<MediaResponse> updateAltText(@PathVariable Long id,
                                                    @RequestParam(required = false) String altText) {
        return ApiResponse.success(mediaService.updateAltText(id, altText));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ApiResponse.success();
    }
}