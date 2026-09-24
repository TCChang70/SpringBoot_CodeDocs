package com.example.cms.user.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
}