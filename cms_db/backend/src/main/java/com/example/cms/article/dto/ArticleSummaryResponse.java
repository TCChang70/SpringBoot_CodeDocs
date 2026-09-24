package com.example.cms.article.dto;

import com.example.cms.article.entity.Article;
import com.example.cms.article.entity.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章回應（列表與詳情共用欄位，detail=true 時含內文）。
 */
public record ArticleSummaryResponse(
        Long id,
        String title,
        String slug,
        String summary,
        ArticleStatus status,
        String featuredImage,
        long viewCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long authorId,
        String authorName,
        List<String> categories,
        List<String> tags,
        String content
) {
    public static ArticleSummaryResponse from(Article article, boolean withContent) {
        return new ArticleSummaryResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getStatus(),
                article.getFeaturedImage(),
                article.getViewCount() == null ? 0 : article.getViewCount(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getAuthor().getId(),
                displayName(article),
                article.getCategories().stream().map(c -> c.getName()).collect(Collectors.toList()),
                article.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()),
                withContent ? article.getContent() : null);
    }

    private static String displayName(Article article) {
        String name = article.getAuthor().getDisplayName();
        return name == null || name.isBlank() ? article.getAuthor().getUsername() : name;
    }
}