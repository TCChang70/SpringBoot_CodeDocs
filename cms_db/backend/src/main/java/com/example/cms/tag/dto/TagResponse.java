package com.example.cms.tag.dto;

import com.example.cms.tag.entity.Tag;

public record TagResponse(
        Integer id,
        String name,
        String slug,
        long articleCount
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getSlug(),
                tag.getArticles() == null ? 0 : tag.getArticles().size());
    }
}