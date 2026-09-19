package com.restaurant.pos.dto.auth;

public record LoginResponse(
        Long id,
        String username,
        String name,
        String role,
        Boolean active) {
}