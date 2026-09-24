package com.example.cms.user.dto;

import com.example.cms.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 管理者更新使用者（FR-01-05）：可變更角色／信箱／顯示名稱。
 */
public record UpdateUserRequest(
        UserRole role,

        @Email(message = "信箱格式不正確")
        @Size(max = 255)
        String email,

        @Size(max = 100, message = "顯示名稱過長")
        String displayName
) {
}