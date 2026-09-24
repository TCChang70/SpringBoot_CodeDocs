package com.example.cms.user.dto;

import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;

import java.time.LocalDateTime;

/**
 * 使用者資料回應（不回傳 password_hash）。
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        UserRole role,
        String avatarUrl,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getAvatarUrl(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}