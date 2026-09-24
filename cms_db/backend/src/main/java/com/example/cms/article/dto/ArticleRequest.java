package com.example.cms.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * 文章建立／更新請求（FR-02-01 / FR-02-10）。
 */
public record ArticleRequest(
        @NotBlank(message = "標題不可為空")
        @Size(max = 255, message = "標題過長")
        String title,

        @NotBlank(message = "slug 不可為空")
        @Size(max = 255, message = "slug 過長")
        String slug,

        @Size(max = 500)
        String summary,

        @NotBlank(message = "內文不可為空")
        String content,

        String featuredImage,

        Set<Integer> categoryIds,

        Set<Integer> tagIds
) {
}