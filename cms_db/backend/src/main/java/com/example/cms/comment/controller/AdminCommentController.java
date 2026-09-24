package com.example.cms.comment.controller;

import com.example.cms.comment.dto.CommentResponse;
import com.example.cms.comment.entity.CommentStatus;
import com.example.cms.comment.service.CommentService;
import com.example.cms.common.ApiResponse;
import com.example.cms.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留言審核端點（FR-05-05）。
 */
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('admin','editor')")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<PageResult<CommentResponse>> list(@RequestParam(required = false) CommentStatus status,
                                                         @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(commentService.listAdmin(status, pageable)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<CommentResponse> approve(@PathVariable Long id) {
        return ApiResponse.success(commentService.approve(id));
    }

    @PatchMapping("/{id}/spam")
    public ApiResponse<CommentResponse> markSpam(@PathVariable Long id) {
        return ApiResponse.success(commentService.markSpam(id));
    }

    @PatchMapping("/{id}/restore")
    public ApiResponse<CommentResponse> restore(@PathVariable Long id) {
        return ApiResponse.success(commentService.restore(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ApiResponse.success();
    }
}