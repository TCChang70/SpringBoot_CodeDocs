package com.restaurant.pos.dto.employee;

import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String username,
        String name,
        String role,
        Boolean active,
        LocalDateTime createdAt) {
}