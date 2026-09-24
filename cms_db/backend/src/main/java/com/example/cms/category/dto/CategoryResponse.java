package com.example.cms.category.dto;

import com.example.cms.category.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 分類回應：含子分類（遞迴）與文章數量，可直接組合成樹。
 */
public record CategoryResponse(
        Integer id,
        String name,
        String slug,
        String description,
        Integer parentId,
        long articleCount,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category category) {
        return from(category, true);
    }

    public static CategoryResponse from(Category category, boolean includeChildren) {
        List<CategoryResponse> childNodes = new ArrayList<>();
        if (includeChildren && category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                childNodes.add(from(child, true));
            }
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() == null ? null : category.getParent().getId(),
                category.getArticles() == null ? 0 : category.getArticles().size(),
                childNodes);
    }
}