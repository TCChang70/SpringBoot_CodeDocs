package com.example.cms.article.controller;

import com.example.cms.article.dto.ArticleSummaryResponse;
import com.example.cms.article.entity.ArticleStatus;
import com.example.cms.article.service.ArticleService;
import com.example.cms.common.ApiResponse;
import com.example.cms.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 後台文章管理端點（SD §4.2 / §5.2）。
 */
@RestController
@RequestMapping("/api/v1/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<PageResult<ArticleSummaryResponse>> list(@RequestParam(required = false) ArticleStatus status,
                                                                @RequestParam(required = false) String keyword,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(articleService.listAdmin(status, keyword, pageable)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('admin','editor','author')")
    public ApiResponse<PageResult<ArticleSummaryResponse>> mine(@RequestParam(required = false) ArticleStatus status,
                                                                @RequestParam(required = false) String keyword,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(articleService.listMine(status, keyword, pageable)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('admin','editor')")
    public ApiResponse<PageResult<ArticleSummaryResponse>> pending(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(articleService.listAdmin(ArticleStatus.pending_review, null, pageable)));
    }
}