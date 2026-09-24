package com.example.cms.article.controller;

import com.example.cms.article.dto.ArticleRequest;
import com.example.cms.article.dto.ArticleSummaryResponse;
import com.example.cms.article.service.ArticleService;
import com.example.cms.common.ApiResponse;
import com.example.cms.common.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/**
 * 文章端點（FR-02）：前台公開瀏覽 + 作者／審核操作。
 */
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<PageResult<ArticleSummaryResponse>> list(@RequestParam(required = false) Integer categoryId,
                                                                @RequestParam(required = false) Integer tagId,
                                                                @RequestParam(required = false) String keyword,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(articleService.searchPublished(categoryId, tagId, keyword, pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleSummaryResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(articleService.getPublished(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','editor','author')")
    public ApiResponse<ArticleSummaryResponse> create(@Valid @RequestBody ArticleRequest request) {
        return ApiResponse.success(articleService.create(request));
    }

    @GetMapping("/{id}/manage")
    public ApiResponse<ArticleSummaryResponse> manage(@PathVariable Long id) {
        return ApiResponse.success(articleService.getManage(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleSummaryResponse> update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ApiResponse.success(articleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ArticleSummaryResponse> submit(@PathVariable Long id) {
        return ApiResponse.success(articleService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ArticleSummaryResponse> approve(@PathVariable Long id) {
        return ApiResponse.success(articleService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ArticleSummaryResponse> reject(@PathVariable Long id) {
        return ApiResponse.success(articleService.reject(id));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<ArticleSummaryResponse> archive(@PathVariable Long id) {
        return ApiResponse.success(articleService.archive(id));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ArticleSummaryResponse> publish(@PathVariable Long id) {
        return ApiResponse.success(articleService.publishAgain(id));
    }
}