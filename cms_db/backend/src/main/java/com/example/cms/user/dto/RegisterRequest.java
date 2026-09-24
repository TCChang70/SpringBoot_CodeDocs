package com.example.cms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 註冊請求（FR-01-01）。
 */
public record RegisterRequest(
        @NotBlank(message = "帳號不可為空")
        @Size(min = 3, max = 50, message = "帳號長度需介於 3~50")
        String username,

        @NotBlank(message = "信箱不可為空")
        @Email(message = "信箱格式不正確")
        String email,

        @NotBlank(message = "密碼不可為空")
        @Size(min = 8, max = 72, message = "密碼長度需介於 8~72")
        String password
) {
}