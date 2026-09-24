package com.example.cms.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 個人資料更新（FR-01-06）。
 */
public record UpdateProfileRequest(
        @Size(max = 100, message = "顯示名稱過長")
        String displayName,

        @Size(max = 500, message = "頭像網址過長")
        String avatarUrl
) {
}