package com.example.cms.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 分類建立／更新請求（FR-03）。
 */
public record CategoryRequest(
        @NotBlank(message = "分類名稱不可為空")
        @Size(max = 100, message = "分類名稱過長")
        String name,

        @NotBlank(message = "slug 不可為空")
        @Size(max = 100, message = "slug 過長")
        String slug,

        @Size(max = 500)
        String description,

        Integer parentId
) {
}