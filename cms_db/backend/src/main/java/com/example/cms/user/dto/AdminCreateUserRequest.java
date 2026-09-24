package com.example.cms.user.dto;

import com.example.cms.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 管理者建立新帳戶：可指定帳號／信箱／密碼／顯示名稱／角色。
 */
public record AdminCreateUserRequest(
        @NotBlank(message = "帳號不可為空")
        @Size(min = 3, max = 50, message = "帳號長度需介於 3~50")
        String username,

        @NotBlank(message = "信箱不可為空")
        @Email(message = "信箱格式不正確")
        @Size(max = 255, message = "信箱過長")
        String email,

        @NotBlank(message = "密碼不可為空")
        @Size(min = 8, max = 72, message = "密碼長度需介於 8~72")
        String password,

        @Size(max = 100, message = "顯示名稱過長")
        String displayName,

        @NotNull(message = "請選擇角色")
        UserRole role
) {
}