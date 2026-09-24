package com.example.cms.comment.controller;

import com.example.cms.comment.dto.CommentRequest;
import com.example.cms.comment.dto.CommentResponse;
import com.example.cms.comment.service.CommentService;
import com.example.cms.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 留言公開端點（FR-05）。
 */
@RestController
@RequestMapping("/api/v1/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long articleId) {
        return ApiResponse.success(commentService.listApproved(articleId));
    }

    @PostMapping
    public ApiResponse<CommentResponse> create(@PathVariable Long articleId,
                                               @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success(commentService.create(articleId, request));
    }
}