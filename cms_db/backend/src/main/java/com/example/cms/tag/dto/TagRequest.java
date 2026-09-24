package com.example.cms.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "標籤名稱不可為空")
        @Size(max = 50, message = "標籤名稱過長")
        String name,

        @NotBlank(message = "slug 不可為空")
        @Size(max = 50, message = "slug 過長")
        String slug
) {
}